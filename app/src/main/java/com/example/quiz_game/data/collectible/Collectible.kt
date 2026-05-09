package com.example.quiz_game.data.collectible

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Collectible(
    var uid: String = "",
    var symbol: String? = null,
    var name: String? = null,
    var description: String? = null,
    var type: CollectibleType? = null,
    var tradeValue: Int? = null, // can be sold for some coins (tradeValue < price)
    var price: Int? = null,
    var createdAt: Long? = null, // in db
    var acquiredAt: Long? = null
) {
    fun generateUid(): String = Base64.encodeToString(
        (symbol + name + description + type)
            .split("")
            .shuffled()
            .joinToString("")
            .toByteArray(),
        Base64.NO_WRAP
    )
}

enum class CollectibleType {
    TROPHY, UTILITY
}
