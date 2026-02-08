package com.example.quiz_game.data.category

import androidx.compose.ui.util.fastMap
import com.example.quiz_game.App
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object CategoryRepository {
    private val mutex = Mutex()

    private suspend fun getRemote(): Boolean {
        var success = false
        runWithTimeout(
            block = {
                val response = Service.categoryService.get()

                if (response.isSuccessful) {
                    val body = response.body()

                    body?.let {
                        insert(
                            *(it.triviaCategories.fastMap { category ->
                                if (category.name != null) category.name = Utils.decodeHtml(category.name!!)
                                category.uid = category.generateUid()
                                category
                            }).toTypedArray()
                        )
                        success = true
                    }
                }
            },
            onFinish = { },
            onTimeout = { }
        )
        return success
    }

    private suspend fun insert(vararg category: Category) {
        runWithTimeout(
            block = { App.db.categoryDao().insert(*category) },
            onFinish = { },
            onTimeout = { }
        )
    }

    suspend fun get(onSuccess: (List<Category>) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                var data: List<Category>
                var fetchError: Throwable? = null

                mutex.withLock {
                    data = App.db.categoryDao().get()

                    if (data.isEmpty()) {
                        val success = getRemote()
                        if (success) {
                            data = App.db.categoryDao().get()
                        } else {
                            fetchError = Exception("Failed to fetch categories from remote")
                        }
                    }
                }

                if (fetchError != null) {
                    onError(fetchError)
                } else {
                    onSuccess(data)
                }
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getByUid(uid: String, onSuccess: (Category) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.categoryDao().getByUid(uid)

                if (data == null) {
                    onError(Exception("Category with id $uid was not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getById(id: Int, onSuccess: (Category) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.categoryDao().getById(id)

                if (data == null) {
                    onError(Exception("Category with id $id was not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getByName(
        name: String,
        onSuccess: (Category) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                val data = App.db.categoryDao().getByName(name)

                if (data == null) {
                    onError(Exception("Category with name $name was not found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun truncate(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { App.db.categoryDao().truncate() },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }
}