package com.example.quiz_game

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Room
import com.example.quiz_game.data.Database
import com.example.quiz_game.other.LocaleHelper
import com.example.quiz_game.other.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.Locale

class App : Application() {
    private val TAG = "test1234 App"

    companion object {
        val ioScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
        lateinit var instance: App
        lateinit var db: Database
        lateinit var userPrefs: SharedPreferences
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

        userPrefs = getSharedPreferences("user-preferences", MODE_PRIVATE)
        if (!userPrefs.contains("lastKnownLanguage")) {
            userPrefs.edit {
                putString(
                    "lastKnownLanguage",
                    if (Utils.supportedLanguage(Locale.getDefault().language)) Locale.getDefault().language else "en"
                )
                commit()
            }
        }
        if (!userPrefs.contains("selectedLanguage")) {
            userPrefs.edit {
                putString(
                    "selectedLanguage",
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
        val prefs = base?.getSharedPreferences("user-preferences", MODE_PRIVATE)
        val lang = prefs?.getString("selectedLanguage", "en") ?: "en"
        val newBase = LocaleHelper.wrap(base!!, lang)
        super.attachBaseContext(newBase)
    }

}

interface AppDestination