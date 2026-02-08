package com.example.quiz_game.data.quiz

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.Service
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object QuizRepository {
    private const val TAG = "test1234 QuizRepository"
    private val mutex = Mutex()

    private suspend fun getRemote(
        amount: Int,
        category: Int? = null,
    ): Boolean {
        var success = false
        runWithTimeout(
            block = {
                val response = Service.quizService.get(amount, category)

                if (response.isSuccessful) {
                    val body = response.body()

                    body?.let {
                        if (it.responseCode == 0) {
                            insert(
                                *it.results.toTypedArray()
                            )
                            success = true
                        }
                    }
                }
            },
            onFinish = {},
            onTimeout = {}
        )
        return success
    }

    private suspend fun insert(vararg quiz: Quiz) {
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
                        if (it.category != null) {
                            it.category = Utils.decodeHtml(it.category!!)
                            Repository.categoryRepository.getByName(
                                Utils.decodeHtml(it.category!!),
                                onSuccess = { category ->
                                    it.categoryUid = category.uid
                                },
                                onError = { /* Ignore error, just keep null categoryUid */ }
                            )
                        }
                        it.uid = it.generateUid()
                        it
                    }).toTypedArray())
            },
            onFinish = {},
            onTimeout = {}
        )
    }

    suspend fun get(
        amount: Int = Constants.DEFAULT_QUIZ_AMOUNT,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                var data: List<Quiz>
                var fetchError: Throwable? = null

                mutex.withLock {
                    data = App.db.quizDao().get()

                    if (data.fastFilter { !it.expired }.size <= amount / 2) {
                        val remaining = amount - data.size
                        val success = getRemote(amount = remaining)
                        if (success) {
                            data = App.db.quizDao().get()
                        } else if (data.isEmpty()) {
                             fetchError = Exception("Failed to fetch quizzes from remote")
                        }
                    }
                }

                if (fetchError != null && data.isEmpty()) {
                     onError(fetchError)
                } else {
                    onSuccess(data)
                }
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
        categoryUid: String,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                Repository.categoryRepository.getByUid(
                    categoryUid,
                    onSuccess = { category ->
                        App.ioScope.launch {
                             processGetByCategory(amount, category, onSuccess, onError)
                        }
                    },
                    onError = onError
                )
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    private suspend fun processGetByCategory(
        amount: Int,
        category: Category,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: List<Quiz> = emptyList()
        var fetchError: Throwable? = null

        mutex.withLock {
             category.name?.let { name ->
                data = App.db.quizDao().getByCategory(name)

                if (data.size < amount) {
                     val remaining = amount - data.size
                     val success = getRemote(amount = remaining, category = category.id)
                     if (success) {
                         data = App.db.quizDao().getByCategory(name)
                     } else if (data.isEmpty()) {
                         fetchError = Exception("Failed to fetch quizzes for category ${category.name}")
                     }
                }
            }
        }
        
        if (fetchError != null && data.isEmpty()) {
            onError(fetchError)
        } else {
            onSuccess(data)
        }
    }

    suspend fun getBySession(
        uids: List<String>,
        onSuccess: (List<Quiz>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                val data = App.db.quizDao().getBySession(uids)

                if (data.isEmpty()) {
                    onError(Exception("One or more quizzes in uids $uids were not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = {},
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