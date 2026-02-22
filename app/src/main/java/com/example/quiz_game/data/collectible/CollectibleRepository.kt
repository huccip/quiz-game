package com.example.quiz_game.data.collectible

import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.other.Utils.readJsonRaw

object CollectibleRepository {

    private suspend fun readCollectiblesRaw(): List<Collectible> {
        return readJsonRaw(inputStream = App.instance.resources.openRawResource(R.raw.collectibles))
    }

    suspend fun get(): List<Collectible> {
        val data = readCollectiblesRaw()
        if (data.isEmpty()) throw Exception("No collectibles were found")
        return data
    }

    suspend fun getByUid(uid: String): Collectible {
        val data = readCollectiblesRaw()
        return data.find { it.uid == uid } ?: throw Exception("No collectible with uid $uid")
    }

    suspend fun getByType(type: CollectibleType): Collectible {
        val data = readCollectiblesRaw()
        return data.find { it.type == type } ?: throw Exception("No collectible with type $type")
    }

    suspend fun getByPriceRange(minCoins: Int = 0, maxCoins: Int? = null): Collectible {
        val data = readCollectiblesRaw()
        return data.find {
            maxCoins?.let { max -> it.price in minCoins..max } ?: run { it.price!! >= minCoins }
        }
                ?: throw Exception("No collectible with price between $minCoins and $maxCoins")
    }
}
