package com.example.quiz_game.data.collectible

import com.example.quiz_game.App
import com.example.quiz_game.other.Utils.readJsonRaw
import com.example.quiz_game.other.Utils.runWithTimeout
import java.io.InputStream

object CollectibleRepository {
    private suspend fun readCollectiblesRaw(
        inputStream: InputStream,
        onSuccess: suspend (List<Collectible>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: List<Collectible> = emptyList()
        runWithTimeout(
            block = {
                readJsonRaw<List<Collectible>>(
                    inputStream = inputStream,
                    onFinish = { data = it },
                    onError = onError
                )
            },
            onFinish = {
                if (data.isEmpty()) {
                    onError(Exception("No raw collectibles were found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
            onTimeout = onError
        )
    }

    suspend fun insert(
        inputStream: InputStream,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = {
                get(
                    onSuccess = {
                        if (it.isNotEmpty()) return@get

                        readCollectiblesRaw(
                            inputStream = inputStream,
                            onSuccess = { collectibles ->
                                if (it.containsAll(collectibles.toList())) return@readCollectiblesRaw

                                App.db.collectibleDao().insert(
                                    *collectibles.map {
                                        it.createdAt = System.currentTimeMillis()
                                        it.uid = it.generateUid()
                                        it
                                    }.toTypedArray()
                                )
                            },
                            onError = onError
                        )

                        onError(Exception("Something went wrong or no collectibles found"))
                    },
                    onError = onError
                )
            },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun get(
        onSuccess: suspend (List<Collectible>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: List<Collectible> = emptyList()
        runWithTimeout(
            block = { data = App.db.collectibleDao().get() },
            onFinish = { onSuccess(data) },
            onTimeout = onError
        )
    }

    suspend fun getByUid(
        uid: String,
        onSuccess: (Collectible) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: Collectible? = null
        runWithTimeout(
            block = { data = App.db.collectibleDao().getByUid(uid) },
            onFinish = {
                if (data == null) {
                    onError(Exception("No collectible with uid $uid"))
                    return@runWithTimeout
                }

                onSuccess(data!!)
            },
            onTimeout = onError
        )
    }

    suspend fun truncate(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        runWithTimeout(
            block = { App.db.collectibleDao().delete() },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }

    suspend fun deleteByUid(uid: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        runWithTimeout(
            block = { App.db.collectibleDao().deleteByUid(uid) },
            onFinish = onSuccess,
            onTimeout = onError
        )
    }
}