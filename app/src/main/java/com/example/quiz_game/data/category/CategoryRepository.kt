package com.example.quiz_game.data.category

import com.example.quiz_game.App
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object CategoryRepository {

    private val fetchMutex = Mutex()

    suspend fun get(): List<Category> =
            withContext(Dispatchers.IO) {
                var data = App.db.categoryDao().get()
                if (data.isEmpty()) {
                    fetchMutex.withLock {
                        // Re-check after acquiring lock — another coroutine may
                        // have
                        // already populated the DB while we were waiting.
                        data = App.db.categoryDao().get()
                        if (data.isEmpty()) {
                            fetchRemote()
                            data = App.db.categoryDao().get()
                        }
                    }
                }
                data
            }

    private suspend fun fetchRemote() =
            withContext(Dispatchers.IO) {
                val response = Service.categoryService.get()
                if (!response.isSuccessful)
                        throw Exception("Failed to fetch categories: ${response.code()}")
                val body = response.body() ?: throw Exception("Category response body is null")
                val categories =
                        body.triviaCategories.map { cat ->
                            cat.copy(
                                    name = cat.name?.let { Utils.decodeHtml(it) },
                                    uid = cat.generateUid()
                            )
                        }
                App.db.categoryDao().insert(*categories.toTypedArray())
            }

    suspend fun getByUid(uid: String): Category =
            withContext(Dispatchers.IO) {
                App.db.categoryDao().getByUid(uid)
                        ?: throw Exception("Category with uid $uid was not found")
            }

    suspend fun getById(id: Int): Category =
            withContext(Dispatchers.IO) {
                App.db.categoryDao().getById(id)
                        ?: throw Exception("Category with id $id was not found")
            }

    suspend fun getByName(name: String): Category? =
            withContext(Dispatchers.IO) { App.db.categoryDao().getByName(name) }

    suspend fun truncate() = withContext(Dispatchers.IO) { App.db.categoryDao().truncate() }
}
