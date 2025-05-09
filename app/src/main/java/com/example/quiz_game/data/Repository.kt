package com.example.quiz_game.data

import android.util.Log
import com.example.quiz_game.App.Companion.ioScope
import com.example.quiz_game.App.Companion.userPrefs
import com.example.quiz_game.data.category.CategoryRepository
import com.example.quiz_game.data.quiz.QuizRepository
import com.example.quiz_game.data.session.SessionRepository
import com.example.quiz_game.other.Constants
import com.example.quiz_game.other.Utils
import com.example.quiz_game.other.Utils.runWithTimeout
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Locale

object Repository {

    private const val TAG = "test1234 Repository"

    suspend fun prepareTranslator(
        onSuccess: (Translator?) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        runWithTimeout(
            block = {
                val supportedLanguages = Constants.SUPPORTED_LANGUAGES.map {
                    it.first
                }
                if (userPrefs.contains("selectedLanguage") || supportedLanguages.contains(Locale.getDefault().language)) {
                    ioScope.launch {
                        try {
                            async {
                                Utils.prepareTranslator(
                                    sourceLanguage = userPrefs.getString("lastKnownLanguage", null)
                                        ?: TranslateLanguage.ENGLISH,
                                    targetLanguage = userPrefs.getString("selectedLanguage", null)
                                        ?: Locale.getDefault().language,
                                    onSuccess = {
                                        Log.d(TAG, "prepareTranslator: $it")
                                        onSuccess(it)
                                    },
                                    onError = onError
                                )
                            }.await()
                            Log.d("test1234 App", "Translator ready 🎯")
                        } catch (e: Exception) {
                            Log.e("test1234 App", "Translation prep failed ⛔", e)
                            onError
                        }
                    }

                    return@runWithTimeout
                }

                onSuccess(null)
            },
            onFinish = {},
            onTimeout = onError
        )
    }

    val quizRepository by lazy { QuizRepository }
    val categoryRepository by lazy { CategoryRepository }
    val sessionRepository by lazy { SessionRepository }
}