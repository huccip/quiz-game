package com.example.quiz_game.data.user

import android.util.Base64
import androidx.compose.ui.util.fastJoinToString
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    var uid: String = "",
    var nickname: String? = null,
    var coins: Int = 0, // global earned score translates to coins
) {
    fun hasSufficientCoins(storePrice: Int): Boolean = (coins >= storePrice)

    fun generateUid(): String = Base64.encodeToString(
        (nickname + coins + UUID.randomUUID().mostSignificantBits)
            .split("")
            .shuffled()
            .fastJoinToString("")
            .toByteArray(),
        Base64.CRLF
    )
}

@Entity(tableName = "collectibles")
data class Collectible(
    @PrimaryKey(autoGenerate = false)
    var uid: String = "",
    var symbol: String? = null,
    var name: String? = null,
    var description: String? = null,
    var type: CollectibleType? = null,
    var tradeValue: Int? = null, // can be sold for some coins
) {
    fun generateUid(): String = Base64.encodeToString(
        (symbol + name + description + type + tradeValue + UUID.randomUUID().mostSignificantBits)
            .split("")
            .shuffled()
            .fastJoinToString("")
            .toByteArray(),
        Base64.CRLF
    )
}

enum class CollectibleType {
    TROPHY, UTILITY
}

@Entity(
    tableName = "user_collectible_relation",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userUid"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Collectible::class,
            parentColumns = ["uid"],
            childColumns = ["collectibleUid"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class UserCollectibleRelation(
    @PrimaryKey(autoGenerate = false)
    val uid: String = "",
    val userUid: String,
    val collectibleUid: String
) {
    fun generateUid(): String = Base64.encodeToString(
        (userUid + collectibleUid + UUID.randomUUID().mostSignificantBits)
            .split("")
            .shuffled()
            .fastJoinToString("")
            .toByteArray(),
        Base64.CRLF
    )
}

data class UserWithCollectibles(
    @Embedded val user: User,
    @Relation(
        parentColumn = "uid",
        entityColumn = "uid",
        associateBy = Junction(UserCollectibleRelation::class)
    )
    val collectibles: List<Collectible>
)