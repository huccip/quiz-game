package com.example.quiz_game.data.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val nickname: String? = null,
    val coins: Int = 0, // global earned score translates to coins
    val createdAt: Long? = null,
    val collectiblesUids: List<String> = emptyList<String>()
)