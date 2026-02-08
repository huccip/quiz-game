package com.example.quiz_game.other

import com.google.mlkit.nl.translate.TranslateLanguage

object Constants {
    const val DEFAULT_QUIZ_AMOUNT = 50
    const val DEFAULT_QUIZ_SESSION_AMOUNT = 2
    const val DEFAULT_QUIZ_TIMER = 21 // seconds
    val SUPPORTED_LANGUAGES = arrayOf(
        TranslateLanguage.ARABIC to "مصر العربية" too "EG",
        TranslateLanguage.GERMAN to "Deutschland Deutsch" too "DE",
        TranslateLanguage.RUSSIAN to "Россия Pусский" too "RU",
        TranslateLanguage.ENGLISH to "USA English" too "US",
        TranslateLanguage.FRENCH to "France Français" too "FR",
        TranslateLanguage.SPANISH to "España Español" too "ES",
    )
}