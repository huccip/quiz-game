package com.example.quiz_game

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.LocaleHelper
import com.example.quiz_game.other.Utils

open class BaseActivity: ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = Repository.getUser()?.language ?: "en"
        val context = LocaleHelper.wrap(newBase, lang)
        super.attachBaseContext(if (Utils.hasInternet() && lang != "en") context else newBase)
    }
}