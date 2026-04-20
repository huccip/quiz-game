package com.example.quiz_game.data.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    var username: String? = null,
    var coins: Int = 0, // global earned score translates to coins
    var createdAt: Long = System.currentTimeMillis(),
    var collectiblesUids: List<String> = emptyList(),
    var achievementsUidsSet: Set<String> = emptySet(),
    var language: String? = null,
    var onboarded: Boolean = false,
    var translatorReady: Boolean = false,
) {
    companion object {
        const val KEY_USER = "user"
        const val KEY_USERNAME = "username"
        const val KEY_COINS = "coins"
        const val KEY_CREATED_AT = "createdAt"
        const val KEY_COLLECTIBLES_UIDS = "collectiblesUids"
        const val KEY_SELECTED_LANGUAGE = "selectedLanguage"
        const val KEY_LAST_KNOWN_LANGUAGE = "lastKnownLanguage"
        const val KEY_ONBOARDED = "onboarded"
    }
}