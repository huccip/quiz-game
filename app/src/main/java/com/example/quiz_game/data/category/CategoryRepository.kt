package com.example.quiz_game.data.category

import androidx.compose.ui.util.fastMap
import com.example.quiz_game.App
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils.runWithTimeout
import kotlinx.coroutines.launch

object CategoryRepository {
    private suspend fun getRemote(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val response = Service.categoryService.get()

                if (response.isSuccessful) {
                    val body = response.body()

                    body?.let {
                        insert(
                            *(it.triviaCategories.fastMap {
                                it.uid = it.generateUid()
                                it
                            }).toTypedArray(),
                            onSuccess = onSuccess,
                            onError = onError
                        )
                        onSuccess()
                    } ?: onError(Exception("Response body was found null"))
                } else {
                    onError(Exception("Response was not successful ${response.errorBody()}"))
                }
            },
            onFinish = { },
            onTimeout = onError
        )
    }

    private suspend fun insert(
        vararg category: Category,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                App.db.categoryDao().insert(*category)
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun get(onSuccess: (List<Category>) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = {
                val data = App.db.categoryDao().get()

                if (data.isEmpty()) {
                    getRemote(
                        onSuccess = {
                            App.ioScope.launch {
                                get(onSuccess, onError)
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

    suspend fun getByUid(uid: String, onSuccess: (Category) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { onSuccess(App.db.categoryDao().getByUid(uid)) },
            onFinish = {},
            onTimeout = onError
        )
    }

    suspend fun getById(id: Int, onSuccess: (Category) -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { onSuccess(App.db.categoryDao().getById(id)) },
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
            block = { onSuccess(App.db.categoryDao().getByName(name)) },
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