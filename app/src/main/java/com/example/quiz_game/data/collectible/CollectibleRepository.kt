package com.example.quiz_game.data.collectible

import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Utils.readJsonRaw
import com.example.quiz_game.other.Utils.runWithTimeout
import java.io.InputStream

object CollectibleRepository {
    private suspend fun readCollectiblesRaw(
        onSuccess: suspend (List<Collectible>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: List<Collectible> = emptyList()
        runWithTimeout(
            block = {
                readJsonRaw<List<Collectible>>(
                    inputStream = App.instance.resources.openRawResource(R.raw.collectibles),
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

    suspend fun get(
        onSuccess: suspend (List<Collectible>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: List<Collectible> = emptyList()
        runWithTimeout(
            block = {
                readCollectiblesRaw(
                    onSuccess = {
                        data = it
                    },
                    onError = onError
                )
            },
            onFinish = {
                if (data.isEmpty()) {
                    onError(Exception("No collectibles were found"))
                    return@runWithTimeout
                }

                onSuccess(data)
            },
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
            block = {
                readCollectiblesRaw(
                    onSuccess = { collectibles ->
                        data = collectibles.find { it.uid == uid }
                    },
                    onError = onError
                )
            },
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

    suspend fun getByType(
        type: CollectibleType,
        onSuccess: (Collectible) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: Collectible? = null

        runWithTimeout(
            block = {
                readCollectiblesRaw(
                    onSuccess = { collectibles ->
                        data = collectibles.find { it.type == type }
                    },
                    onError = onError
                )
            },
            onFinish = {
                if (data == null) {
                    onError(Exception("No collectible with type $type"))
                    return@runWithTimeout
                }

                onSuccess(data!!)
            },
            onTimeout = onError
        )
    }

    suspend fun getByPriceRange( //this can serve as getAll with minCoins = 0 and maxCoins = null
        minCoins: Int = 0,
        maxCoins: Int? = null,
        onSuccess: (Collectible) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        var data: Collectible? = null

        runWithTimeout(
            block = {
                readCollectiblesRaw(
                    onSuccess = { collectibles ->
                        data = collectibles.find {
                            maxCoins?.let { max ->
                                it.price in minCoins..max
                            } ?: run { it.price!! >= minCoins }
                        }
                    },
                    onError = onError
                )
            },
            onFinish = {
                if (data == null) {
                    onError(Exception("No collectible with price between $minCoins and $maxCoins"))
                    return@runWithTimeout
                }

                onSuccess(data!!)
            },
            onTimeout = onError
        )
    }
}