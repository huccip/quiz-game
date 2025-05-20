package com.example.quiz_game

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.room.Room
import com.example.quiz_game.data.Database
import com.example.quiz_game.data.Repository.getUser
import com.example.quiz_game.data.Repository.saveUser
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.LocaleHelper
import com.example.quiz_game.other.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import java.util.Locale

private const val TAG = "test1234 App"

class App : Application() {

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

        userPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
    }

    override fun onTerminate() {
        super.onTerminate()
        ioScope.cancel("The application was terminated and the ioScope is cancelled")
    }
}

interface AppDestination