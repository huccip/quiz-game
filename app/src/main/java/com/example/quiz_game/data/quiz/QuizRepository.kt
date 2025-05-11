package com.example.quiz_game.data.quiz

import android.util.Log
import androidx.compose.ui.util.fastMap
import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object QuizRepository {
    private const val TAG = "test1234 QuizRepository"
    private suspend fun getRemote(
        amount: Int,
        category: Int? = null,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                val response = Service.quizService.get(amount, category)

                if (response.isSuccessful) {
                    val body = response.body()

                    body?.let {
                        if (it.responseCode == 0) {
                            insert(
                                *it.results.toTypedArray(),
                                onSuccess = onSuccess,
                                onError = onError
                            )
                        }

                        onError(Exception(it.mapResponseCodeToReadableInfo()))
                    } ?: onError(Exception("Response body was found null"))
                } else {
                    onError(Exception("Response was not successful ${response.errorBody()}"))
                }
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    private suspend fun insert(
        vararg quiz: Quiz,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                App.db.quizDao()
                    .insert(*(quiz.map {
                        it.mark = it.generateMark()
                        if (it.question != null) it.question = Utils.decodeHtml(it.question!!)
                        if (it.correctAnswer != null) it.correctAnswer =
                            Utils.decodeHtml(it.correctAnswer!!)
                        if (it.incorrectAnswers != null) {
                            it.incorrectAnswers!!.fastMap { Utils.decodeHtml(it) }
                        }
                        it.uid = it.generateUid()
                        it
                    }).toTypedArray())
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun get(
        amount: Int = 50,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                val data = App.db.quizDao().get()
                if (data.isEmpty()) {
                    getRemote(
                        amount = amount,
                        onSuccess = {
                            App.ioScope.launch {
                                // ⚠ careful
                                delay(1000L)
                                get(amount, onSuccess, onError)
                            }
                        },
                        onError = onError
                    )
                } else if (data.size < amount / 2) {
                    getRemote(
                        amount = amount / 2,
                        onSuccess = {
                            App.ioScope.launch {
                                // ⚠ careful
                                delay(1000L)
                                get(amount / 2, onSuccess, onError)
                            }
                        },
                        onError = onError
                    )
                }
                onSuccess(data)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getByUid(uid: String, onSuccess: (Quiz) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.quizDao().getByUid(uid)

                if (data == null) {
                    onError(Exception("Quiz with uid $uid was not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getByCategory(
        amount: Int = 50,
        category: String,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                Repository.categoryRepository.getByName(
                    category,
                    onSuccess = { category ->
                        category.name?.let { name ->
                            val data = App.db.quizDao().getByCategory(name)

                            if (data.isEmpty()) {
                                App.ioScope.launch {
                                    getRemote(
                                        amount = amount,
                                        category = category.id,
                                        onSuccess = {
                                            App.ioScope.launch {
                                                // ⚠ careful
                                                delay(1000L)
                                                getByCategory(amount, name, onSuccess, onError)
                                            }
                                        },
                                        onError = onError
                                    )
                                }
                            } else if (data.size < amount / 2) {
                                App.ioScope.launch {
                                    getRemote(
                                        amount = amount / 2,
                                        category = category.id,
                                        onSuccess = {
                                            App.ioScope.launch {
                                                // ⚠ careful
                                                delay(1000L)
                                                getByCategory(amount / 2, name, onSuccess, onError)
                                            }
                                        },
                                        onError = onError
                                    )
                                }
                            }
                            onSuccess(data)
                        } ?: onError(Exception("Category name was found null"))
                    },
                    onError = onError
                )
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getBySession(
        uids: List<String>,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data = emptyList<Quiz>()
        runWithTimeout(
            block = { data = App.db.quizDao().getBySession(uids) },
            onFinish = {
                if (data.isEmpty()) {
                    onError(Exception("One or more quizzes in uids $uids were not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onTimeout = onError
        )
    }

    suspend fun deleteByUid(uid: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = { quiz ->
                        if (!quiz.expired) {
                            onError(Exception("Quiz with uid ${quiz.uid} has not expired yet"))
                            return@getByUid
                        }

                        App.db.quizDao().deleteByUid(quiz.uid)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun truncate(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { App.db.quizDao().truncate() },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun updateExpired(uid: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                getByUid(
                    uid = uid,
                    onSuccess = {
                        App.db.quizDao().updateExpired(it.uid)
                        onSuccess()
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }
}