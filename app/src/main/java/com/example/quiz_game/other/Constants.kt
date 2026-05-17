package com.example.quiz_game.other

import com.google.mlkit.nl.translate.TranslateLanguage

private infix fun <A, B, C> Pair<A, B>.too(c: C): Triple<A, B, C> =
        Triple(this.first, this.second, c)

object Constants {
    const val DEFAULT_QUIZ_AMOUNT = 50
    const val DEFAULT_QUIZ_SESSION_AMOUNT = 10
    const val DEFAULT_QUIZ_TIMER = 21 // seconds
    const val DEFAULT_TRIVIA_API_RETRY_DELAY = 5000L // millis
    const val DEFAULT_REWARD_AMOUNT = 25
    const val DEFAULT_DAILY_STREAK_REWARD_TIMER = 7
    val SUPPORTED_LANGUAGES =
            arrayOf(
                    TranslateLanguage.ARABIC to "مصر العربية" too "EG",
                    TranslateLanguage.GERMAN to "Deutschland Deutsch" too "DE",
                    TranslateLanguage.RUSSIAN to "Россия Pусский" too "RU",
                    TranslateLanguage.ENGLISH to "USA English" too "US",
                    TranslateLanguage.FRENCH to "France Français" too "FR",
                    TranslateLanguage.SPANISH to "España Español" too "ES",
            )
}
