package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.other.Constants
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuizViewModel : ViewModel() {
    var state = MutableStateFlow(QuizState())
        private set

    fun onAction(action: QuizAction) {
        viewModelScope.launch {
            when (action) {
                is QuizAction.GetAll -> execute {
                    Repository.quizRepository.get(
                        amount = Constants.DEFAULT_QUIZ_AMOUNT,
                        onSuccess = { updateStateOnSuccess(list = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is QuizAction.GetByCategory -> execute {
                    Repository.quizRepository.getByCategory(
                        amount = Constants.DEFAULT_QUIZ_AMOUNT,
                        category = action.category,
                        onSuccess = { updateStateOnSuccess(list = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is QuizAction.GetByUid -> execute {
                    Repository.quizRepository.getByUid(
                        uid = action.uid,
                        onSuccess = { updateStateOnSuccess(data = it) },
                        onError = { updateStateOnError(it) }
                    )
                }

                is QuizAction.GetBySession -> execute {
                    val translatedQuizzes = action.uids.map { uid ->
                        viewModelScope.async(Dispatchers.IO) {
                            var fetchedQuiz: Quiz? = null
                            Repository.quizRepository.getByUid(
                                uid = uid,
                                onSuccess = { quiz -> fetchedQuiz = quiz },
                                onError = { e ->
                                    updateStateOnError(e)
                                    null
                                }
                            )
                            fetchedQuiz?.let { quiz ->
                                action.translator?.let { translator ->
                                    try {
                                        val translatedQuestion =
                                            translator.translate(quiz.question ?: "").await()
                                        val translatedCorrectAnswer =
                                            translator.translate(quiz.correctAnswer ?: "").await()
                                        val translatedIncorrectAnswers =
                                            quiz.incorrectAnswers?.map { answer ->
                                                translator.translate(answer).await()
                                            }
                                        quiz.copy(
                                            question = translatedQuestion,
                                            correctAnswer = translatedCorrectAnswer,
                                            incorrectAnswers = translatedIncorrectAnswers
                                        )
                                    } catch (e: Exception) {
                                        updateStateOnError(e)
                                        quiz
                                    }
                                } ?: quiz
                            }
                        }
                    }.mapNotNull { deferred -> deferred.await() }

                    updateStateOnSuccess(sessionList = translatedQuizzes)
                }

                is QuizAction.DeleteByUid -> execute {
                    Repository.quizRepository.deleteByUid(
                        uid = action.uid,
                        onSuccess = { updateStateOnSuccess() },
                        onError = { updateStateOnError(it) }
                    )
                }

                is QuizAction.UpdateExpired -> execute {
                    Repository.quizRepository.updateExpired(
                        uid = action.uid,
                        onSuccess = { onAction(QuizAction.GetAll) },
                        onError = { updateStateOnError(it) }
                    )
                }
            }
        }
    }

    private suspend fun execute(block: suspend () -> Unit) {
        state.value = state.value.copy(executing = true)

        delay(1000L)
        block()
    }

    private fun updateStateOnError(e: Throwable) {
        state.value = state.value.copy(
            executing = false,
            errors = state.value.errors.apply { add(e) }
        )
    }

    private fun updateStateOnSuccess(
        data: Quiz? = null,
        list: List<Quiz>? = null,
        sessionList: List<Quiz>? = null
    ) {
        state.value = state.value.copy(executing = false)
        data?.let { state.value = state.value.copy(quiz = data) }
        list?.let { state.value = state.value.copy(quizzes = list) }
        sessionList?.let { state.value = state.value.copy(sessionQuizzes = sessionList) }
    }
}

data class QuizState(
    var executing: Boolean = false,
    var errors: ArrayList<Throwable> = arrayListOf(),
    var quizzes: List<Quiz> = emptyList(),
    var sessionQuizzes: List<Quiz> = emptyList(),
    var quiz: Quiz = Quiz()
)

sealed interface QuizAction {
    data object GetAll : QuizAction
    data class GetByCategory(val category: String) : QuizAction
    data class GetByUid(val uid: String) : QuizAction
    data class GetBySession(val uids: List<String>, val translator: Translator?) : QuizAction
    data class DeleteByUid(val uid: String) : QuizAction
    data class UpdateExpired(val uid: String) : QuizAction
}