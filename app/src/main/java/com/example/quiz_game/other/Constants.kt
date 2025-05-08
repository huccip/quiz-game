package com.example.quiz_game.other

import com.google.mlkit.nl.translate.TranslateLanguage

object Constants {
    const val DEFAULT_QUIZ_AMOUNT = 50
    const val DEFAULT_QUIZ_SESSION_AMOUNT = 2
    const val DEFAULT_QUIZ_TIMER = 21 // seconds
    val SUPPORTED_LANGUAGES = arrayOf(
        TranslateLanguage.ARABIC to "مصر" too "EG",
        TranslateLanguage.GERMAN to "Deutschland" too "DE",
        TranslateLanguage.RUSSIAN to "Россия" too "RU",
        TranslateLanguage.ENGLISH to "United States" too "US",
        TranslateLanguage.FRENCH to "France" too "FR",
        TranslateLanguage.SPANISH to "España" too "ES",
    )
}