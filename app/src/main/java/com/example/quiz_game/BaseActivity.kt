package com.example.quiz_game

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.quiz_game.other.LocaleHelper

open class BaseActivity: ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("user-preferences", MODE_PRIVATE)
        val lang = prefs?.getString("selectedLanguage", "en") ?: "en"
        val context = LocaleHelper.wrap(newBase, lang)
        super.attachBaseContext(context)
    }
}