package com.example.quiz_game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.TranslatorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuizViewModel : ViewModel() {
    var state = MutableStateFlow(QuizState())
        private set

    init {
        onAction(QuizAction.GetAll)
    }

    fun onAction(action: QuizAction) {
        viewModelScope.launch {
            when (action) {
                is QuizAction.GetAll ->
                        execute {
                            val quizzes =
                                    Repository.quizRepository.get(
                                            amount = Constants.DEFAULT_QUIZ_AMOUNT
                                    )
                            state.value = state.value.copy(quizzes = quizzes, ready = true)
                        }
                is QuizAction.GetByCategory ->
                        execute {
                            val quizzes =
                                    Repository.quizRepository.getByCategory(
                                            amount = Constants.DEFAULT_QUIZ_AMOUNT,
                                            categoryUid = action.categoryUid
                                    )
                            state.value = state.value.copy(quizzes = quizzes)
                        }
                is QuizAction.GetByUid ->
                        execute {
                            val quiz = Repository.quizRepository.getByUid(uid = action.uid)
                            state.value = state.value.copy(quiz = quiz)
                        }
                is QuizAction.GetBySession ->
                        execute {
                            val translator = TranslatorManager.translator.value
                            val quizzes =
                                    action.uids
                                            .map { uid ->
                                                async(Dispatchers.IO) {
                                                    val quiz =
                                                            Repository.quizRepository.getByUid(
                                                                    uid = uid
                                                            )
                                                    translator?.let { translateQuiz(quiz, it) }
                                                            ?: quiz
                                                }
                                            }
                                            .awaitAll()
                            state.value = state.value.copy(sessionQuizzes = quizzes)
                        }
                is QuizAction.DeleteByUid -> {
                    Repository.quizRepository.deleteByUid(uid = action.uid)
                }
                is QuizAction.UpdateExpired ->
                        execute {
                            Repository.quizRepository.updateExpired(uid = action.uid)
                            // Refresh list after update
                            val quizzes =
                                    Repository.quizRepository.get(
                                            amount = Constants.DEFAULT_QUIZ_AMOUNT
                                    )
                            state.value = state.value.copy(quizzes = quizzes)
                        }
            }
        }
    }

    private suspend fun translateQuiz(
            quiz: Quiz,
            translator: com.google.mlkit.nl.translate.Translator
    ): Quiz {
        return try {
            quiz.copy(
                    category = quiz.category?.let { translator.translate(it).await() },
                    question = quiz.question?.let { translator.translate(it).await() },
                    correctAnswer = quiz.correctAnswer?.let { translator.translate(it).await() },
                    incorrectAnswers =
                            quiz.incorrectAnswers?.map { translator.translate(it).await() }
            )
        } catch (e: Exception) {
            quiz // fallback to untranslated on failure
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

data class QuizState(
        val executing: Boolean = false,
        val ready: Boolean = false,
        val errors: List<Throwable> = emptyList(),
        val quizzes: List<Quiz> = emptyList(),
        val sessionQuizzes: List<Quiz> = emptyList(),
        val quiz: Quiz = Quiz()
)

sealed interface QuizAction {
    data object GetAll : QuizAction
    data class GetByCategory(val categoryUid: String) : QuizAction
    data class GetByUid(val uid: String) : QuizAction
    data class GetBySession(val uids: List<String>) : QuizAction
    data class DeleteByUid(val uid: String) : QuizAction
    data class UpdateExpired(val uid: String) : QuizAction
}
