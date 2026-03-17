package com.example.quiz_game.data.quiz

import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object QuizRepository {

    private val fetchMutex = Mutex()

    suspend fun get(amount: Int = Constants.DEFAULT_QUIZ_AMOUNT): List<Quiz> =
            withContext(Dispatchers.IO) {
                var data = App.db.quizDao().get()

                if (data.filter { !it.expired }.size <= amount / 2) {
                    fetchMutex.withLock {
                        // Re-check after lock — another coroutine may have fetched already
                        data = App.db.quizDao().get()
                        if (data.filter { !it.expired }.size <= amount / 2) {
                            val remaining = amount - data.size
                            fetchRemote(amount = remaining)
                            data = App.db.quizDao().get()
                        }
                    }
                }

                data
            }

    suspend fun getByUid(uid: String): Quiz =
            withContext(Dispatchers.IO) {
                App.db.quizDao().getByUid(uid)
                        ?: throw Exception("Quiz with uid $uid was not found")
            }

    suspend fun getByCategory(
            amount: Int = Constants.DEFAULT_QUIZ_AMOUNT,
            categoryUid: String
    ): List<Quiz> =
            withContext(Dispatchers.IO) {
                val category =
                        App.db.categoryDao().getByUid(categoryUid)
                                ?: throw Exception("Category with uid $categoryUid was not found")

                var data = App.db.quizDao().getByCategoryUid(category.uid)

                if (data.filter { !it.expired }.size <= amount / 2) {
                    fetchMutex.withLock {
                        // Re-check after lock — another coroutine may have fetched already
                        data = App.db.quizDao().getByCategoryUid(category.uid)
                        if (data.filter { !it.expired }.size <= amount / 2) {
                            val remaining = amount - data.size
                            fetchRemote(amount = remaining, category = category.id)

                            // Fix: re-link any orphaned quizzes whose categoryUid is null
                            // but whose category name matches the target category.
                            // This handles encoding mismatches in insertQuizzes' name lookup.
                            val allQuizzes = App.db.quizDao().get()
                            val orphaned =
                                    allQuizzes.filter {
                                        it.categoryUid == null && it.category == category.name
                                    }
                            if (orphaned.isNotEmpty()) {
                                App.db
                                        .quizDao()
                                        .insert(
                                                *orphaned
                                                        .map { it.copy(categoryUid = category.uid) }
                                                        .toTypedArray()
                                        )
                            }

                            data = App.db.quizDao().getByCategoryUid(category.uid)
                        }
                    }
                }

                data
            }

    suspend fun getBySession(uids: List<String>): List<Quiz> =
            withContext(Dispatchers.IO) {
                val data = App.db.quizDao().getBySession(uids)
                if (data.isEmpty()) {
                    throw Exception("One or more quizzes in uids $uids were not found")
                }
                data
            }

    suspend fun deleteByUid(uid: String) =
            withContext(Dispatchers.IO) {
                val quiz =
                        App.db.quizDao().getByUid(uid)
                                ?: throw Exception("Quiz with uid $uid was not found")
                if (!quiz.expired) {
                    throw Exception("Quiz with uid ${quiz.uid} has not expired yet")
                }
                App.db.quizDao().deleteByUid(quiz.uid)
            }

    suspend fun truncate() = withContext(Dispatchers.IO) { App.db.quizDao().truncate() }

    suspend fun updateExpired(uid: String) =
            withContext(Dispatchers.IO) {
                val quiz =
                        App.db.quizDao().getByUid(uid)
                                ?: throw Exception("Quiz with uid $uid was not found")
                App.db.quizDao().updateExpired(quiz.uid)
            }

    suspend fun getUnexpiredCounts(): Map<String, Int> =
            withContext(Dispatchers.IO) {
                App.db.quizDao().get()
                        .filter { !it.expired && it.categoryUid != null }
                        .groupBy { it.categoryUid!! }
                        .mapValues { it.value.size }
            }

    private suspend fun fetchRemote(amount: Int, category: Int? = null) =
            withContext(Dispatchers.IO) {
                var currentAmount = amount
                while (currentAmount >= Constants.DEFAULT_QUIZ_SESSION_AMOUNT + 10) {
                    val response =
                            async { Service.quizService.get(currentAmount, category) }.await()
                    if (!response.isSuccessful) {
                        throw Exception("API request failed: ${response.code()}")
                    }
                    val body = response.body() ?: throw Exception("Response body is null")

                    when (body.responseCode) {
                        0 -> {
                            insertQuizzes(body.results)
                            return@withContext
                        }
                        1 -> {
                            // Reduce amount by half, but keep at least
                            // Constants.DEFAULT_QUIZ_SESSION_AMOUNT
                            currentAmount -= 10
                            delay(Constants.DEFAULT_TRIVIA_API_RETRY_DELAY)
                        }
                        5 -> {
                            // Rate limited — wait and retry same amount
                            delay(Constants.DEFAULT_TRIVIA_API_RETRY_DELAY * 2)
                        }
                        else -> throw Exception("API error code ${body.responseCode}")
                    }
                }
            }

    private suspend fun insertQuizzes(quizzes: List<Quiz>) =
            withContext(Dispatchers.IO) {
                // Ensure categories are in DB before looking up UIDs
                val existingCategories = App.db.categoryDao().get()
                if (existingCategories.isEmpty()) {
                    // Force-fetch categories first
                    Repository.categoryRepository.get()
                }

                val processed =
                        quizzes.map { quiz ->
                            val decoded =
                                    quiz.copy(
                                            question = quiz.question?.let { Utils.decodeHtml(it) },
                                            correctAnswer =
                                                    quiz.correctAnswer?.let {
                                                        Utils.decodeHtml(it)
                                                    },
                                            incorrectAnswers =
                                                    quiz.incorrectAnswers?.map {
                                                        Utils.decodeHtml(it)
                                                    },
                                            category = quiz.category?.let { Utils.decodeHtml(it) },
                                    )
                            val categoryUid =
                                    decoded.category?.let { name ->
                                        App.db.categoryDao().getByName(name)?.uid
                                    }
                            decoded.copy(
                                    uid = decoded.generateUid(),
                                    mark = decoded.generateMark(),
                                    categoryUid = categoryUid
                            )
                        }
                App.db.quizDao().insert(*processed.toTypedArray())
            }
}
