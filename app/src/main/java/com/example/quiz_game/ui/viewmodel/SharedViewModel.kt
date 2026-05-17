package com.example.quiz_game.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.quiz_game.App
import com.example.quiz_game.AppDestination
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.DailyRewards
import com.example.quiz_game.other.TranslatorManager
import com.example.quiz_game.ui.activity.onboard.OnboardActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    var state = MutableStateFlow(SharedState())
        private set

    init {
        // Seed the user snapshot from disk so screens that read [SharedState.user]
        // (Home's coin pill, daily-reward gating, etc.) have a value on first frame.
        refreshUser()
    }

    fun onAction(action: SharedAction) {
        viewModelScope.launch {
            when (action) {
                is SharedAction.Navigate<*> -> {
                    delay(100L)
                    action.navController.navigate(action.destination)
                }
                is SharedAction.NavigateBack -> {
                    delay(100L)
                    action.navController.popBackStack()
                }
                is SharedAction.StartActivity<*> -> {
                    action.context.startActivity(Intent(action.context, action.activity))
                    (action.context as ComponentActivity).finish()
                }
                is SharedAction.PrepareTranslator ->
                        execute {
                            // Delegate to TranslatorManager (which manages status updates)
                            TranslatorManager.prepare(action.language)

                            // Restart to apply the correct strings.xml
                            val currentActivity = action.context as? Activity
                            val intent = Intent(action.context, OnboardActivity::class.java)
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                            )
                            action.context.startActivity(intent)
                            currentActivity?.overridePendingTransition(0, 0)
                            currentActivity?.finishAffinity()
                        }
                is SharedAction.Restart ->
                        execute {
                            val currentActivity = action.context as? Activity
                            val intent = Intent(action.context, OnboardActivity::class.java)
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                            )
                            action.context.startActivity(intent)
                            currentActivity?.overridePendingTransition(0, 0)
                            currentActivity?.finishAffinity()
                        }
                is SharedAction.Deeplink ->
                        execute {
                            val intent = Intent(Intent.ACTION_VIEW, action.url.toUri())
                            action.context.startActivity(intent)
                        }
                is SharedAction.RefreshUser -> refreshUser()
                is SharedAction.EvaluateDailyLogin -> evaluateDailyLogin()
                is SharedAction.ClaimDailyLogin -> claimDailyLogin()
                is SharedAction.ConsumedStreakReward -> {
                    state.value = state.value.copy(pendingStreakReward = null)
                }
                is SharedAction.DismissStreakReward -> dismissStreakReward()
                is SharedAction.ClaimLootBox -> claimLootBox()
                is SharedAction.ConsumeLootBoxReward -> {
                    state.value = state.value.copy(pendingLootBoxReward = null)
                }
            }
        }
    }

    /**
     * Re-reads the persisted user blob and emits it on [SharedState.user].
     * Called after every mutation so screens that observe the flow recompose
     * with up-to-date coin / streak / claim values.
     */
    fun refreshUser() {
        state.value = state.value.copy(user = Repository.getUser())
    }

    /**
     * Daily-login streak entry-point. Evaluates the streak on [App.ioScope]
     * (because it touches SharedPreferences) and, if a reward is due, persists
     * the new streak state and stages a one-shot popup payload on
     * [SharedState.pendingStreakReward] for the UI to consume.
     */
    private fun evaluateDailyLogin() {
        App.ioScope.launch {
            val now = System.currentTimeMillis()
            // Snapshot the streak inputs first; the actual mutation happens
            // atomically inside [Repository.updateUser] so we cannot race the
            // loot-box claim (which uses the same mutex).
            val snapshot = Repository.getUser() ?: return@launch
            val outcome = DailyRewards.evaluateLogin(
                now = now,
                lastLoginAt = snapshot.lastLoginAt,
                previousStreakDays = snapshot.loginStreakDays,
            )
            if (outcome.alreadyClaimedToday) return@launch

            val updated = Repository.updateUser { user ->
                // Re-evaluate inside the lock against the freshest user blob,
                // otherwise a concurrent writer (loot box, quiz reward, …)
                // could have already moved [lastLoginAt] forward.
                val freshOutcome = DailyRewards.evaluateLogin(
                    now = now,
                    lastLoginAt = user.lastLoginAt,
                    previousStreakDays = user.loginStreakDays,
                )
                if (freshOutcome.alreadyClaimedToday) return@updateUser user
                user.copy(
                    lastLoginAt = now,
                    loginStreakDays = freshOutcome.streakDays,
                    coins = user.coins + freshOutcome.coinsGranted,
                )
            }

            // If another writer beat us to today's grant, don't surface a popup.
            if (updated.lastLoginAt != now) return@launch

            state.value = state.value.copy(user = updated)
        }
    }

    /**
     * Stages a one-shot daily-login streak reward on
     * [SharedState.pendingStreakReward] without persisting the user blob.
     * Called on Home launch to surface the dialog; the actual persistence
     * (lastLoginAt, streakDays, coins) happens later via [evaluateDailyLogin]
     * once the user watches the rewarded interstitial ad.
     */
    private fun claimDailyLogin() {
        App.ioScope.launch {
            val now = System.currentTimeMillis()
            val snapshot = Repository.getUser() ?: return@launch
            val outcome = DailyRewards.evaluateLogin(
                now = now,
                lastLoginAt = snapshot.lastLoginAt,
                previousStreakDays = snapshot.loginStreakDays,
            )
            if (outcome.alreadyClaimedToday) return@launch

            state.value = state.value.copy(
                pendingStreakReward = StreakRewardEvent(
                    streakDays = outcome.streakDays,
                    coinsGranted = outcome.coinsGranted,
                    wasReset = outcome.wasReset,
                ),
            )
        }
    }

    /**
     * Called when the user explicitly skips the streak dialog (dismiss button).
     * Marks today as "handled" by persisting [lastLoginAt] so
     * [claimDailyLogin] short-circuits on future visits, but does NOT grant
     * coins or increment the streak. The user forfeits the reward by skipping.
     */
    private fun dismissStreakReward() {
        App.ioScope.launch {
            val now = System.currentTimeMillis()
            Repository.updateUser { user ->
                val outcome = DailyRewards.evaluateLogin(now, user.lastLoginAt, user.loginStreakDays)
                if (outcome.alreadyClaimedToday) return@updateUser user
                user.copy(lastLoginAt = now)
            }
            state.value = state.value.copy(pendingStreakReward = null)
        }
    }

    /**
     * Loot-box entry-point. Rolls a reward, persists the coin delta and the new
     * `lastLootBoxClaimAt` atomically (sharing the same mutex as the daily-login
     * flow), and stages the reward on [SharedState.pendingLootBoxReward] for Home
     * to render. The ShopItem grant for power-up rewards is handled by Home via
     * a side effect on the staged event so this VM stays free of shop coupling.
     */
    private fun claimLootBox() {
        App.ioScope.launch {
            val now = System.currentTimeMillis()
            // Pre-flight check using the in-memory snapshot — avoids rolling a
            // reward we'd just throw away. The authoritative gate lives inside
            // the [Repository.updateUser] lambda below.
            val snapshot = Repository.getUser() ?: return@launch
            if (!DailyRewards.lootBoxAvailable(now, snapshot.lastLootBoxClaimAt)) return@launch

            val reward = DailyRewards.rollLootBox()
            val coinDelta = (reward as? DailyRewards.LootBoxReward.Coins)?.amount ?: 0

            val updated = Repository.updateUser { user ->
                // Re-check availability against the freshest user blob to defend
                // against double-taps and against a concurrent writer that might
                // have already claimed today.
                if (!DailyRewards.lootBoxAvailable(now, user.lastLootBoxClaimAt)) return@updateUser user
                user.copy(
                    lastLootBoxClaimAt = now,
                    coins = user.coins + coinDelta,
                )
            }

            if (updated.lastLootBoxClaimAt != now) return@launch

            state.value = state.value.copy(
                user = updated,
                pendingLootBoxReward = reward,
            )
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true, errors = emptyList())
        try {
            block()
        } catch (e: Exception) {
            state.value = state.value.copy(errors = state.value.errors + e)
        } finally {
            state.value = state.value.copy(executing = false)
        }
    }
}

data class SharedState(
        val executing: Boolean = false,
        val errors: List<Throwable> = emptyList(),
        /**
         * Most recent snapshot of the persisted [User]. Updated on every
         * [SharedAction.RefreshUser] dispatch so observing screens recompose
         * when coins / streak / claim timestamps change.
         */
        val user: User? = null,
        /**
         * One-shot payload describing a freshly granted login-streak reward.
         * The Home screen renders a popup as long as this is non-null and
         * fires [SharedAction.ConsumedStreakReward] once the user dismisses
         * it so it doesn't reappear on rotation.
         */
        val pendingStreakReward: StreakRewardEvent? = null,
        /**
         * One-shot payload describing a freshly opened daily loot box. Home
         * renders the reveal dialog while non-null and dispatches
         * [SharedAction.ConsumeLootBoxReward] on dismiss. Power-up grants are
         * applied as a side effect on this state from Home.
         */
        val pendingLootBoxReward: com.example.quiz_game.other.DailyRewards.LootBoxReward? = null,
)

/** Snapshot of a freshly-credited daily-login streak reward. */
data class StreakRewardEvent(
    val streakDays: Int,
    val coinsGranted: Int,
    val wasReset: Boolean,
)

sealed interface SharedAction {
    data class Navigate<T : AppDestination>(val destination: T, val navController: NavController) :
            SharedAction

    data class NavigateBack(val navController: NavController) : SharedAction
    data class StartActivity<T : ComponentActivity>(val context: Context, val activity: Class<T>) :
            SharedAction

    data class PrepareTranslator(val language: String, val context: Context) : SharedAction
    data class Restart(val context: Context) : SharedAction

    data class Deeplink(val context: Context, val url: String) : SharedAction

    /** Re-read the persisted user from disk. Dispatch after any coin/state mutation. */
    data object RefreshUser : SharedAction

    /** Evaluate the daily-login streak; may stage a [StreakRewardEvent] for Home. */
    data object EvaluateDailyLogin : SharedAction
    data object ClaimDailyLogin : SharedAction

    /** Mark the staged streak-reward event as consumed (Home uses this on dismiss). */
    data object ConsumedStreakReward : SharedAction

    /** Skip the streak dialog without granting reward but mark today as handled. */
    data object DismissStreakReward : SharedAction

    /** Roll + persist a daily loot-box reward atomically; stages a reveal payload. */
    data object ClaimLootBox : SharedAction

    /** Mark the staged loot-box reward as consumed (Home uses this on dismiss). */
    data object ConsumeLootBoxReward : SharedAction
}
