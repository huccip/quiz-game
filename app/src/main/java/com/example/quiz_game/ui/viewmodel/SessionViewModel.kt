package com.example.quiz_game.ui.viewmodel

import androidx.compose.ui.util.fastFirstOrNull
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.session.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val TAG = "test1234 SessionViewModel"

class SessionViewModel : ViewModel() {
    var state = MutableStateFlow(SessionState())
        private set

    init {
        onAction(SessionAction.GetAll)
    }

    fun onAction(action: SessionAction) {
        viewModelScope.launch {
            when (action) {
                is SessionAction.GetAll -> execute {
                    Repository.sessionRepository.get(
                        onSuccess = {
                            updateStateOnSuccess(
                                list = it,
                                data = it.fastFirstOrNull { it.expiredAt == null }
                            )
                        },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SessionAction.GetByUid -> execute {
                    Repository.sessionRepository.getByUid(
                        uid = action.uid,
                        onSuccess = { updateStateOnSuccess(data = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SessionAction.InitiateSession -> execute {
                    Repository.sessionRepository.insert(
                        session = arrayOf(
                            Session(
                                quizzesUids = action.quizzesUids,
                                maxScore = action.maxScore,
                            )
                        ),
                        onSuccess = { updateStateOnSuccess(data = it.fastFirstOrNull { it.expiredAt == null }) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SessionAction.EndSession -> execute {
                    Repository.sessionRepository.updateExpiredAt(
                        uid = action.uid,
                        expiredAt = System.currentTimeMillis(),
                        onSuccess = { updateStateOnSuccess(data = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SessionAction.UpdateScore -> execute {
                    Repository.sessionRepository.updateScore(
                        uid = action.uid,
                        score = (state.value.session.score ?: 0) + action.score,
                        onSuccess = { onAction(SessionAction.GetByUid(action.uid)) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is SessionAction.UpdateTrophies -> execute {
                    val achievements = arrayListOf<Int>()

                    // record trophies
                    state.value.session.score?.let {
                        if (it > App.userPrefs.getInt("high_score", 0)) {
                            achievements.add(R.string.achievements_new_record)
                            App.userPrefs.edit {
                                putInt("high_score", it)
                                commit()
                            }
                        }
                    }
                    // mistakes trophies
                    achievements.add(
                        when (action.incorrectlyAnswered) {
                            0 -> R.string.achievements_no_mistakes
                            1 -> R.string.achievements_one_mistake
                            2 -> R.string.achievements_two_mistakes
                            10 -> R.string.achievements_ten_mistakes
                            20 -> R.string.achievements_ten_mistakes
                            else -> R.string.achievements_empty
                        }
                    )

                    // timelapse trophies
                    state.value.session.timelapse?.let {
                        achievements.add(
                            when (it) {
                                in 0..59_999L -> R.string.achievement_timelapse_quick_thinker
                                in 60_000L..599_999L -> R.string.achievement_timelapse_casual_cruiser
                                in 600_000L..1_199_999L -> R.string.achievement_timelapse_steady_strategist
                                in 1_200_000L..1_799_999L -> R.string.achievement_timelapse_brain_marathoner
                                in 1_800_000L..2_699_999L -> R.string.achievement_timelapse_quiz_zen_master
                                in 2_700_000L..3_599_999L -> R.string.achievement_timelapse_time_bender
                                else -> R.string.achievement_timelapse_eternal_quizzer
                            }
                        )
                    }
                }

            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true)

        delay(500L)
        block()
    }

    private fun updateStateOnSuccess(
        data: Session? = null,
        list: List<Session>? = null
    ) {
        data?.let { state.value = state.value.copy(session = data) }
        list?.let { state.value = state.value.copy(sessions = list) }
        state.value = state.value.copy(executing = false)
    }

    private fun updateStateOnError(throwable: Throwable) {
        state.value = state.value.copy(
            errors = state.value.errors.apply { add(throwable) },
            executing = false
        )
    }
}

data class SessionState(
    val executing: Boolean = false,
    val errors: ArrayList<Throwable> = arrayListOf(),
    val session: Session = Session(), // this will be our reference for the current app run
    val sessions: List<Session> = emptyList<Session>(),
)

sealed interface SessionAction {
    data class InitiateSession(
        val quizzesUids: List<String>,
        val maxScore: Int
    ) : SessionAction

    data object GetAll : SessionAction
    data class GetByUid(val uid: String) : SessionAction
    data class UpdateScore(val uid: String, val score: Int) : SessionAction
    data class UpdateTrophies(val uid: String, val incorrectlyAnswered: Int) : SessionAction
    data class EndSession(val uid: String) : SessionAction
}