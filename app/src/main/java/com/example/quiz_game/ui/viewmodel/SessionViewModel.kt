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
                                                state.value = state.value.copy(session = session)
                                        }
                                is SessionAction.UpdateScore ->
                                        execute {
                                                val currentSession =
                                                        state.value.session ?: return@execute
                                                val newScore =
                                                        (currentSession.score ?: 0) + action.score
                                                val session =
                                                        Repository.sessionRepository.updateScore(
                                                                uid = action.uid,
                                                                score = newScore
                                                        )
                                                state.value = state.value.copy(session = session)
                                        }
                                is SessionAction.UpdateTrophies -> {
                                        val currentSession = state.value.session ?: return@launch
                                        val achievements = mutableListOf<Int>()

                                        // first session ever
                                        if (state.value.sessions.isEmpty()) {
                                                achievements.add(R.string.achievements_first_session)
                                        }

                                        // new high score
                                        currentSession.score?.let {
                                                if (it > App.userPrefs.getInt("high_score", 0)) {
                                                        achievements.add(
                                                                R.string.achievements_new_record
                                                        )
                                                        App.userPrefs.edit {
                                                                putInt("high_score", it)
                                                                commit()
                                                        }
                                                }
                                        }

                                        // mistake-based achievement
                                        val mistakeAchievement = when {
                                                action.incorrectlyAnswered == 0 ->
                                                        R.string.achievements_no_mistakes
                                                action.incorrectlyAnswered == 1 ->
                                                        R.string.achievements_one_mistake
                                                action.incorrectlyAnswered == 2 ->
                                                        R.string.achievements_two_mistakes
                                                action.incorrectlyAnswered >= 10 ->
                                                        R.string.achievements_rough_session
                                                (currentSession.score ?: 0) == 0 ->
                                                        R.string.achievements_rough_session
                                                else -> null
                                        }
                                        mistakeAchievement?.let { achievements.add(it) }

                                        // timelapse achievement (4 tiers)
                                        currentSession.timelapse?.let {
                                                achievements.add(
                                                        when (it) {
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

                                        state.value =
                                                state.value.copy(
                                                        session =
                                                                currentSession.copy(
                                                                        achievements = achievements
                                                                )
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
}
