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

    // ── Retention bookkeeping ─────────────────────────────────────────────
    /**
     * Epoch-millis at which the user last claimed the daily loot box.
     * `0L` means it has never been claimed; the box is available again
     * once the local-calendar day has rolled over since this stamp.
     */
    var lastLootBoxClaimAt: Long = 0L,

    /**
     * Epoch-millis of the user's last "login" (i.e. the last time the
     * Home screen was opened on a given calendar day, used for the
     * consecutive-day streak counter). `0L` means the streak has never
     * been credited yet.
     */
    var lastLoginAt: Long = 0L,

    /**
     * Current consecutive-day login streak. Increments by 1 each time the
     * user opens the app the next calendar day after [lastLoginAt]; resets
     * to 1 if the gap is 2+ days.
     */
    var loginStreakDays: Int = 0,

    /**
     * Epoch-millis of the user's last completed quiz session. Used to grant
     * the silent "first quiz of the day" 2x bonus — if no session was
     * completed earlier on the current calendar day, the upcoming session
     * is the first-of-day and qualifies.
     */
    var lastQuizCompletedAt: Long = 0L,
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
