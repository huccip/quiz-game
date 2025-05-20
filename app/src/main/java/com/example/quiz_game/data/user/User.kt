package com.example.quiz_game.data.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var username: String? = null,
    var coins: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var collectiblesUids: List<String> = emptyList<String>(),
    var language: String? = null,
    var onboarded: Boolean = false,
) {
    companion object {
        const val KEY_USER = "user"
    }
}