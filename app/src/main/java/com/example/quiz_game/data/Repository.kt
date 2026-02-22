package com.example.quiz_game.data

import android.util.Log
import androidx.core.content.edit
import com.example.quiz_game.App.Companion.userPrefs
import com.example.quiz_game.data.category.CategoryRepository
import com.example.quiz_game.data.collectible.CollectibleRepository
import com.example.quiz_game.data.quiz.QuizRepository
import com.example.quiz_game.data.quote.QuoteRepository
import com.example.quiz_game.data.session.SessionRepository
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.Utils
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import kotlinx.serialization.json.Json

object Repository {

    private const val TAG = "Repository"

    suspend fun prepareTranslator(): Translator {
        val userLanguage =
            getUser()?.language
                ?: throw IllegalStateException(
                    "Language is null, maybe the user has not set the language yet?"
                )

        val translator =
            Utils.prepareTranslator(
                sourceLanguage = TranslateLanguage.ENGLISH,
                targetLanguage = userLanguage
            )

        getUser()?.let { user ->
            user.translatorReady = true
            saveUser(user)
        }

        return translator
    }

    fun saveUser(user: User) {
        userPrefs.edit {
            putString(User.KEY_USER, Json.encodeToString(user))
            commit()
        }
    }

    fun getUser(): User? {
        val userJson = userPrefs.getString(User.KEY_USER, null)
        return userJson?.let {
            try {
                Json.decodeFromString<User>(it)
            } catch (e: Exception) {
                Log.e(TAG, "getUser: Could not retrieve user info $e")
                null
            }
        }
    }

    val quizRepository by lazy { QuizRepository }
    val categoryRepository by lazy { CategoryRepository }
    val sessionRepository by lazy { SessionRepository }
    val collectibleRepository by lazy { CollectibleRepository }
    val quoteRepository by lazy { QuoteRepository }
}
