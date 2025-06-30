package com.example.quiz_game.data.category

import androidx.compose.ui.util.fastMap
import com.example.quiz_game.App
import com.example.quiz_game.data.Service
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

object CategoryRepository {
    private val mutex = Mutex()
    private suspend fun getRemote(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        var response: Response<TriviaCategory>? = null

        runWithTimeout(
            block = { response = Service.categoryService.get() },
            onFinish = {
                val currentResponse = response
                if (currentResponse == null) {
                    onError(Exception("Something went wrong trying to read categories from server"))
                    return@runWithTimeout
                }

                if (!currentResponse.isSuccessful) {
                    onError(Exception("Response was not successful ${currentResponse.errorBody()}"))
                    return@runWithTimeout
                }

                val body = currentResponse.body()

                body?.let {
                    insert(
                        *(it.triviaCategories.fastMap {
                            if (it.name != null) it.name = Utils.decodeHtml(it.name!!)
                            it.uid = it.generateUid()
                            it
                        }).toTypedArray(),
                        onSuccess = onSuccess,
                        onError = onError
                    )
                } ?: onError(Exception("Response body was found null"))
            },
            onTimeout = onError
        )
    }

    private suspend fun insert(
        vararg category: Category,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = { App.db.categoryDao().insert(*category) },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun get(onSuccess: (List<Category>) -> Unit, onError: (Throwable) -> Unit) {
        var localData: List<Category>? = null

        runWithTimeout(
            block = { localData = App.db.categoryDao().get() },
            onFinish = {
                val currentLocalData = localData
                if (currentLocalData == null) {
                    onError(Exception("Something went wrong trying to read categories from database"))
                    return@runWithTimeout
                }

                if (currentLocalData.isEmpty()) {
                    App.ioScope.launch {
                        mutex.withLock {
                            val dataAfterLock = App.db.categoryDao().get()
                            if (dataAfterLock.isNotEmpty()) {
                                onSuccess(dataAfterLock)
                            } else {
                                getRemote(
                                    onSuccess = {
                                        App.ioScope.launch {
                                            val finalData = App.db.categoryDao().get()
                                            onSuccess(finalData)
                                        }
                                    },
                                    onError = onError
                                )
                            }
                        }
                    }
                    return@runWithTimeout
                }

                onSuccess(currentLocalData)
            },
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