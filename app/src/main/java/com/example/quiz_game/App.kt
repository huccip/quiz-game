package com.example.quiz_game

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Room
import com.example.quiz_game.data.Database
import com.example.quiz_game.data.quote.Quote
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.LocaleHelper
import com.example.quiz_game.other.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.Locale

class App : Application() {


    companion object {
        val ioScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
        lateinit var instance: App
        lateinit var db: Database
        lateinit var userPrefs: SharedPreferences
        lateinit var quotePrefs: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        db = Room
            .databaseBuilder(
                this.applicationContext,
                Database::class.java,
                "quiz-game.db"
            )
            .build()

        quotePrefs = getSharedPreferences(Quote.KEY_QUOTE, MODE_PRIVATE)
        userPrefs = getSharedPreferences(User.KEY_USER, MODE_PRIVATE)
        if (!userPrefs.contains(User.KEY_LAST_KNOWN_LANGUAGE)) {
            userPrefs.edit {
                putString(
                    User.KEY_LAST_KNOWN_LANGUAGE,
                    if (Utils.supportedLanguage(Locale.getDefault().language)) Locale.getDefault().language else "en"
                )
                commit()
            }
        }
        if (!userPrefs.contains(User.KEY_SELECTED_LANGUAGE)) {
            userPrefs.edit {
                putString(
                    User.KEY_SELECTED_LANGUAGE,
                    if (Utils.supportedLanguage(Locale.getDefault().language)) Locale.getDefault().language else "en"
                )
                commit()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        ioScope.cancel("The application was terminated and the ioScope is cancelled")
    }

    override fun attachBaseContext(base: Context?) {
        val prefs = base?.getSharedPreferences(User.KEY_USER, MODE_PRIVATE)
        val lang = prefs?.getString(User.KEY_SELECTED_LANGUAGE, "en") ?: "en"
        val newBase = LocaleHelper.wrap(base!!, lang)
        super.attachBaseContext(newBase)
    }

}

interface AppDestination