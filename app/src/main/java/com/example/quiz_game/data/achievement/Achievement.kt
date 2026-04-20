package com.example.quiz_game.data.achievement

import android.util.Base64
import androidx.compose.ui.util.fastJoinToString
import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val uid: String = "",
    val name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val createdAt: Long? = null, // in db
    val achievedAt: Long? = null
) {
    fun generateUid(): String = Base64.encodeToString(
        (createdAt.toString() + name + description + icon)
            .split("")
            .shuffled()
            .fastJoinToString("")
            .toByteArray(),
        Base64.NO_WRAP
    )
}