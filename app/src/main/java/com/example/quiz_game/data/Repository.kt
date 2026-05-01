package com.example.quiz_game.data

import android.util.Log
import androidx.core.content.edit
import com.example.quiz_game.App.Companion.quotePrefs
import com.example.quiz_game.App.Companion.userPrefs
import com.example.quiz_game.data.category.CategoryRepository
import com.example.quiz_game.data.collectible.CollectibleRepository
import com.example.quiz_game.data.quiz.QuizRepository
import com.example.quiz_game.data.quote.Quote
import com.example.quiz_game.data.quote.QuoteRepository
import com.example.quiz_game.data.session.SessionRepository
import com.example.quiz_game.data.user.User
import com.example.quiz_game.other.Utils
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object Repository {

    /**
     * Guards every read-modify-write on the persisted [User] blob. SharedPreferences
     * stores the user as a single serialized JSON string, so any flow that loads,
     * mutates and saves it must hold this lock — otherwise concurrent writers (e.g.
     * the daily-streak coroutine and a user-driven loot-box claim) clobber each
     * other's fields with stale snapshots.
     */
    private val userMutex = Mutex()

    /**
     * Atomically read the current [User], apply [transform] and persist the result.
     * All callers that mutate the user blob MUST go through this method to avoid
     * lost-update races. If no user exists yet, it creates a fresh [User] before
     * applying the transform.
     */
    suspend fun updateUser(transform: (User) -> User): User = userMutex.withLock {
        val current = getUser() ?: User()
        val updated = transform(current)
        saveUser(updated)
        updated
    }

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

        updateUser { it.copy(translatorReady = true) }

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
                Log.e("Repository", "getUser: Could not retrieve user info $e")
                null
            }
        }
    }

    fun saveQuote(quote: Quote) {
        quotePrefs.edit {
            putString(Quote.KEY_QUOTE, Json.encodeToString(quote))
            commit()
        }
    }

    fun getQuote(): Quote? {
        val quoteJson = quotePrefs.getString(Quote.KEY_QUOTE, null)
        return quoteJson?.let {
            try {
                Json.decodeFromString<Quote>(it)
            } catch (e: Exception) {
                Log.e("Repository", "getQuote: Could not retrieve quote info $e")
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
