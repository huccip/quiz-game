package com.example.quiz_game.data

import android.util.Log
import androidx.core.content.edit
import com.example.quiz_game.App.Companion.ioScope
import com.example.quiz_game.App.Companion.userPrefs
import com.example.quiz_game.data.category.CategoryRepository
import com.example.quiz_game.data.collectible.CollectibleRepository
import com.example.quiz_game.data.quiz.QuizRepository
import com.example.quiz_game.data.quote.QuoteRepository
import com.example.quiz_game.data.session.SessionRepository
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale

object Repository {

    private const val TAG = "test1234 Repository"

    suspend fun prepareTranslator(
        onSuccess: (Translator) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        runWithTimeout(
            block = {
                val userLanguage = getUser()?.language
                if (userLanguage != null) {
                    ioScope.launch {
                        try {
                            async {
                                Utils.prepareTranslator(
                                    sourceLanguage = TranslateLanguage.ENGLISH,
                                    targetLanguage = userLanguage,
                                    onSuccess = {
                                        getUser()?.let {
                                            it.translatorReady = true
                                            saveUser(it)
                                        }
                                        onSuccess(it)
                                    },
                                    onError = onError
                                )
                            }.await()
                        } catch (e: Exception) {
                            onError(e)
                        }
                    }

                    return@runWithTimeout
                }

                onError(IllegalStateException("Language is null, maybe the user has not set the language yet?"))
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    fun saveUser(user: User) {
        userPrefs.edit {
            putString(User.KEY_USER, Json.encodeToString(user))
            apply()
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