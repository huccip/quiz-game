package com.example.quiz_game.ui.viewmodel

import androidx.compose.ui.util.fastFirstOrNull
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.session.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SessionViewModel : ViewModel() {
    var state = MutableStateFlow(SessionState())
        private set

    init {
        onAction(SessionAction.GetAll)
    }

    fun onAction(action: SessionAction) {
        viewModelScope.launch {
            when (action) {
                is SessionAction.GetAll ->
                    execute {
                        val sessions = Repository.sessionRepository.get()
                        val active =
                            sessions.fastFirstOrNull {
                                it.expiredAt == null
                            }
                        state.value =
                            state.value.copy(
                                sessions = sessions,
                                session = active
                            )
                    }

                is SessionAction.GetByUid ->
                    execute {
                        val session =
                            Repository.sessionRepository.getByUid(
                                action.uid
                            )
                        state.value = state.value.copy(session = session)
                    }

                is SessionAction.InitiateSession ->
                    execute {
                        val sessions =
                            Repository.sessionRepository.insert(
                                Session(
                                    quizzesUids =
                                        action.quizzesUids,
                                    maxScore = action.maxScore,
                                )
                            )
                        val active =
                            sessions.fastFirstOrNull {
                                it.expiredAt == null
                            }
                        state.value = state.value.copy(session = active)
                    }

                is SessionAction.EndSession ->
                    execute {
                        val session =
                            Repository.sessionRepository
                                .updateExpiredAt(
                                    uid = action.uid,
                                    expiredAt =
                                        System.currentTimeMillis()
                                )
                        // Refresh the sessions list so Home stats update
                        // immediately without needing an app restart.
                        val sessions = Repository.sessionRepository.get()
                        state.value =
                            state.value.copy(
                                session = session,
                                sessions = sessions
                            )
                    }

                is SessionAction.UpdateScore ->
                    execute {
                        //update user coins in the process
                        Repository.updateUser { it.copy(coins = it.coins + action.score) }

                        val currentSession =
                            state.value.session ?: return@execute
                        val newScore =
                            (currentSession.score ?: 0) + action.score
                        val session =
                            Repository.sessionRepository.updateScore(
                                uid = action.uid,
                                score = newScore
                            )
                        // Keep sessions list in sync so Home stats (best
                        // score, totals) reflect the latest score.
                        val sessions = Repository.sessionRepository.get()
                        state.value =
                            state.value.copy(
                                session = session,
                                sessions = sessions
                            )
                    }

                is SessionAction.UpdateTrophies -> {
                    val currentSession = state.value.session ?: return@launch
                    val achievements =
                        computeAchievements(
                            session = currentSession,
                            prevSessions = state.value.sessions,
                            incorrectlyAnswered = action.incorrectlyAnswered
                        )

                    state.value =
                        state.value.copy(
                            session =
                                currentSession.copy(
                                    achievements = achievements
                                )
                        )
                }

                is SessionAction.CompleteSession ->
                    execute {
                        // Snapshot the count of previously-completed sessions
                        // BEFORE we refresh the list. Needed for the "first
                        // session ever" achievement check.
                        val hadPriorSessions =
                            state.value.sessions.isNotEmpty()

                        // 1. Apply the final answer's score (cumulative).
                        val priorScore =
                            state.value.session?.score ?: 0
                        val afterScore =
                            Repository.sessionRepository.updateScore(
                                uid = action.uid,
                                score = priorScore + action.finalMark
                            )
                        // Also credit the coins for the final mark (the
                        // per-answer path in UpdateScore does this, but the
                        // last answer now funnels through CompleteSession).
                        if (action.finalMark != 0) {
                            Repository.updateUser { it.copy(coins = it.coins + action.finalMark) }
                        }

                        // 2. Mark the session expired so `timelapse` is valid.
                        val expired =
                            Repository.sessionRepository
                                .updateExpiredAt(
                                    uid = action.uid,
                                    expiredAt =
                                        System.currentTimeMillis()
                                )

                        // 3. Compute achievements against the fully-
                        // populated session (score + timelapse present).
                        val achievements =
                            computeAchievements(
                                session = expired,
                                prevSessions =
                                    if (hadPriorSessions)
                                        state.value.sessions
                                    else emptyList(),
                                incorrectlyAnswered =
                                    action.incorrectlyAnswered
                            )

                        // 4. Persist achievements so they survive reloads.
                        val finalSession =
                            if (achievements.isNotEmpty()) {
                                Repository.sessionRepository
                                    .updateAchievements(
                                        uid = action.uid,
                                        achievements = achievements
                                    )
                            } else {
                                expired
                            }

                        // 5. Refresh the sessions list so Home stats and
                        // the trophy chip reflect the newly-ended session.
                        val sessions =
                            Repository.sessionRepository.get()
                        state.value =
                            state.value.copy(
                                session = finalSession,
                                sessions = sessions
                            )
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

    /**
     * Compute achievement string-resource IDs for a session based on its
     * final score, timelapse, and the user's answer accuracy.
     *
     * [prevSessions] should be the list of previously-completed sessions
     * (excluding the one being evaluated). If empty, the "first session ever"
     * achievement is awarded.
     */
    private fun computeAchievements(
        session: Session,
        prevSessions: List<Session>,
        incorrectlyAnswered: Int
    ): List<Int> {
        val achievements = mutableListOf<Int>()

        // first session ever
        if (prevSessions.isEmpty()) {
            achievements.add(R.string.achievements_first_session)
        }

        // new high score
        session.score?.let { score ->
            val prevHigh = App.userPrefs.getInt("high_score", 0)
            if (score > prevHigh && score > 0) {
                achievements.add(R.string.achievements_new_record)
                App.userPrefs.edit {
                    putInt("high_score", score)
                    commit()
                }
            }
        }

        // mistake-based achievement
        val mistakeAchievement = when {
            incorrectlyAnswered == 0 ->
                R.string.achievements_no_mistakes

            incorrectlyAnswered == 1 ->
                R.string.achievements_one_mistake

            incorrectlyAnswered == 2 ->
                R.string.achievements_two_mistakes

            incorrectlyAnswered >= 10 ->
                R.string.achievements_rough_session

            (session.score ?: 0) == 0 ->
                R.string.achievements_rough_session

            else -> null
        }
        mistakeAchievement?.let { achievements.add(it) }

        // timelapse achievement (4 tiers)
        session.timelapse?.let { timelapse ->
            achievements.add(
                when (timelapse) {
                    in 0..59_999L ->
                        R.string.achievement_timelapse_quick_thinker

                    in 60_000L..599_999L ->
                        R.string.achievement_timelapse_casual_cruiser

                    in 600_000L..1_799_999L ->
                        R.string.achievement_timelapse_brain_marathoner

                    else ->
                        R.string.achievement_timelapse_eternal_quizzer
                }
            )
        }

        return achievements
    }
}

data class SessionState(
    val executing: Boolean = false,
    val errors: List<Throwable> = emptyList(),
    val session: Session? = null,
    val sessions: List<Session> = emptyList(),
)

sealed interface SessionAction {
    data class InitiateSession(val quizzesUids: List<String>, val maxScore: Int) :
        SessionAction

    data object GetAll : SessionAction
    data class GetByUid(val uid: String) : SessionAction
    data class UpdateScore(val uid: String, val score: Int) : SessionAction
    data class UpdateTrophies(val uid: String, val incorrectlyAnswered: Int) : SessionAction
    data class EndSession(val uid: String) : SessionAction

    /**
     * Finalizes a session atomically: applies the final answer's mark,
     * marks the session expired, computes trophies, persists them, and
     * refreshes the sessions list. Replaces the previously fragmented
     * UpdateScore + UpdateTrophies + EndSession sequence which had race
     * conditions (trophies computed before expiredAt/score were set, and
     * achievements wiped when EndSession refetched the session from DB).
     */
    data class CompleteSession(
        val uid: String,
        val finalMark: Int,
        val incorrectlyAnswered: Int
    ) : SessionAction
}
