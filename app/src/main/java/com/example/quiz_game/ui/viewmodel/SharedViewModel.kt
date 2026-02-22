package com.example.quiz_game.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.quiz_game.AppDestination
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.user.User
import com.example.quiz_game.ui.activity.onboard.OnboardActivity
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    var state = MutableStateFlow(SharedState())
        private set

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
                    println("test1234 SharedViewModel StartActivity()")
                    action.context.startActivity(Intent(action.context, action.activity))
                    (action.context as ComponentActivity).finish()
                }

                is SharedAction.PrepareTranslator ->
                    execute {
                        coroutineScope {
                            // 1. Save language to storage (bypasses OnboardViewModel so
                            //    onboardState.user.language stays null → startDestination
                            //    stays on Language → loading keeps showing)
                            state.value =
                                state.value.copy(translatorStatus = TranslatorStatus.Saving)
                            val user = Repository.getUser() ?: User()
                            Repository.saveUser(user.copy(language = action.language))

                            // 2. Prepare translator (reads saved language from storage)
                            state.value =
                                state.value.copy(
                                    translatorStatus = TranslatorStatus.Downloading
                                )

                            // Escalate status if download takes >8s
                            val timerJob = launch {
                                delay(8000)
                                state.value =
                                    state.value.copy(
                                        translatorStatus = TranslatorStatus.SlowDownload
                                    )
                            }

                            try {
                                val translator =
                                    async { Repository.prepareTranslator() }.await()
                                timerJob.cancel()
                                state.value =
                                    state.value.copy(
                                        translator = translator,
                                        translatorStatus = TranslatorStatus.Restarting
                                    )
                            } catch (e: Exception) {
                                timerJob.cancel()
                                state.value =
                                    state.value.copy(
                                        translatorStatus = TranslatorStatus.Failed
                                    )
                                throw e // let execute() catch it and set errors
                            }

                            println(
                                "test1234 SharedViewModel PrepareTranslator() translator (${state.value.translator}) is ready! 🎉"
                            )

                            // 3. Restart to apply the correct strings.xml
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
            }
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
    val translator: Translator? = null,
    val translatorStatus: TranslatorStatus = TranslatorStatus.Idle
)

enum class TranslatorStatus {
    Idle,
    Saving, // "Saving language..."
    Downloading, // "Downloading language model..."
    SlowDownload, // "Still downloading, please stay connected..."
    Failed, // "Download failed."
    Restarting // "Applying language, restarting..."
}

sealed interface SharedAction {
    data class Navigate<T : AppDestination>(val destination: T, val navController: NavController) :
        SharedAction

    data class NavigateBack(val navController: NavController) : SharedAction
    data class StartActivity<T : ComponentActivity>(val context: Context, val activity: Class<T>) :
        SharedAction

    data class PrepareTranslator(val language: String, val context: Context) : SharedAction
    data class Restart(val context: Context) : SharedAction

    data class Deeplink(val context: Context, val url: String) : SharedAction
}
