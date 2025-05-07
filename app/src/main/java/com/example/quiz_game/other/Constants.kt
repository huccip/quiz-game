package com.example.quiz_game.other

import com.example.quiz_game.R
import com.google.mlkit.nl.translate.TranslateLanguage

object Constants {
    const val DEFAULT_QUIZ_AMOUNT = 50
    val SUPPORTED_LANGUAGES = arrayOf<Triple<String, String, String>>(
        TranslateLanguage.ARABIC to "مصر" too "EG",
        TranslateLanguage.GERMAN to "Deutschland" too "DE",
        TranslateLanguage.RUSSIAN to "Россия" too "RU",
        TranslateLanguage.FRENCH to "France" too "FR",
        TranslateLanguage.SPANISH to "España" too "ES",
    )
    val USER_AVATARS = mapOf<Int, Int>(
        R.drawable.ic_arrow_north_east to R.string.avatar_fire,
        R.drawable.ic_arrow_north_east to R.string.avatar_water,
        R.drawable.ic_arrow_north_east to R.string.avatar_rock,
        R.drawable.ic_arrow_north_east to R.string.avatar_wind,
    )
}