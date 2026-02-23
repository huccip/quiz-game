package com.example.quiz_game

import android.content.Context
import androidx.activity.ComponentActivity
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.LocaleHelper

open class BaseActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = Repository.getUser()?.language
        val context = lang?.let { LocaleHelper.wrap(newBase, lang) } ?: newBase

        super.attachBaseContext(context)
    }
}
