package com.example.quiz_game

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.quiz_game.data.Database
import com.example.quiz_game.data.quote.Quote
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.LocaleHelper
import com.example.quiz_game.other.SoundManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json

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
            .addMigrations(Database.MIGRATION_1_2)
            .build()

        quotePrefs = getSharedPreferences(Quote.KEY_QUOTE, MODE_PRIVATE)
        userPrefs = getSharedPreferences(User.KEY_USER, MODE_PRIVATE)

        // AdMob is initialised from each Activity after UMP consent is resolved.
        SoundManager.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SoundManager.release()
        ioScope.cancel("The application was terminated and the ioScope is cancelled")
    }

    override fun attachBaseContext(base: Context?) {
        val prefs = base?.getSharedPreferences(User.KEY_USER, MODE_PRIVATE)
        val lang: String = try {
            val userJson = prefs?.getString(User.KEY_USER, null)
            if (userJson != null) Json.decodeFromString<User>(userJson).language ?: "en"
            else "en"
        } catch (e: Exception) { "en" }
        val newBase = LocaleHelper.wrap(base!!, lang)
        super.attachBaseContext(newBase)
    }

}

interface AppDestination
