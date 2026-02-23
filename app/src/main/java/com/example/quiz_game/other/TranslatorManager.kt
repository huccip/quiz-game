package com.example.quiz_game.other

import android.util.Log
import com.example.quiz_game.App
import com.example.quiz_game.data.Repository
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import java.io.IOException
import java.net.UnknownHostException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Singleton that manages the ML Kit translator instance. Lives outside ViewModel lifecycles so it
 * survives activity restarts (e.g. after finishAffinity() for locale changes).
 *
 * Usage:
 * - Call [prepare] during onboarding (downloads the model).
 * - Call [initIfReady] on cold start to restore from the cached model.
 * - Observe [translator] / [status] from any ViewModel or Composable.
 */
object TranslatorManager {

    private const val TAG = "TranslatorManager"
    const val TASK_ID = "translator"

    private val _translator = MutableStateFlow<Translator?>(null)
    val translator: StateFlow<Translator?> = _translator.asStateFlow()

    private val _status = MutableStateFlow(TranslatorStatus.Idle)
    val status: StateFlow<TranslatorStatus> = _status.asStateFlow()

    /** The language the user selected but hasn't finished downloading yet. */
    private val _pendingLanguage = MutableStateFlow<String?>(null)
    val pendingLanguage: StateFlow<String?> = _pendingLanguage.asStateFlow()

    /**
     * First-time preparation: downloads the ML Kit model for the given [language]. Shows
     * progressive status updates. Called from the Language screen.
     */
    suspend fun prepare(language: String) {
        _pendingLanguage.value = language

        try {
            // 1. Save language
            _status.value = TranslatorStatus.Saving
            val user = Repository.getUser() ?: com.example.quiz_game.data.user.User()
            Repository.saveUser(user.copy(language = language))

            // 2. Skip download for English → English
            if (language == TranslateLanguage.ENGLISH) {
                Log.d(TAG, "English selected — creating passthrough translator (no download)")
                val translator =
                        Utils.prepareTranslator(
                                sourceLanguage = TranslateLanguage.ENGLISH,
                                targetLanguage = TranslateLanguage.ENGLISH
                        )
                _translator.value = translator
                _pendingLanguage.value = null
                _status.value = TranslatorStatus.Restarting
                NetworkRecoveryManager.removePendingTask(TASK_ID)
                return
            }

            // 3. Download model
            _status.value = TranslatorStatus.Downloading

            // Escalate status if download takes >8s
            val slowJob =
                    App.ioScope.launch {
                        delay(8000)
                        if (_status.value == TranslatorStatus.Downloading) {
                            _status.value = TranslatorStatus.SlowDownload
                        }
                    }

            try {
                val translator = withTimeout(25_000L) { Repository.prepareTranslator() }
                slowJob.cancel()
                _translator.value = translator
                _pendingLanguage.value = null
                _status.value = TranslatorStatus.Restarting
                NetworkRecoveryManager.removePendingTask(TASK_ID)
                Log.d(TAG, "Translator prepared successfully ✅")
            } catch (e: TimeoutCancellationException) {
                slowJob.cancel()
                _status.value = TranslatorStatus.InternetLost
                Log.e(TAG, "Translator download timed out — likely no internet", e)
                NetworkRecoveryManager.addPendingTask(
                        NetworkRecoveryManager.PendingTask(
                                id = TASK_ID,
                                label = "Translation model"
                        ) { prepare(language) }
                )
                throw e
            } catch (e: Exception) {
                slowJob.cancel()

                // Distinguish network errors from other failures
                if (isNetworkError(e)) {
                    _status.value = TranslatorStatus.InternetLost
                    Log.e(TAG, "Translator download interrupted — internet lost", e)
                    // Register for auto-retry when internet returns
                    NetworkRecoveryManager.addPendingTask(
                            NetworkRecoveryManager.PendingTask(
                                    id = TASK_ID,
                                    label = "Translation model"
                            ) { prepare(language) }
                    )
                } else {
                    _status.value = TranslatorStatus.Failed
                    Log.e(TAG, "Translator preparation failed (non-network)", e)
                }
                throw e
            }
        } catch (e: Exception) {
            if (_status.value != TranslatorStatus.Failed &&
                            _status.value != TranslatorStatus.InternetLost
            ) {
                _status.value = TranslatorStatus.Failed
            }
            throw e
        }
    }

    /**
     * Called on cold start (e.g. in MainActivity.onCreate). If the user previously downloaded a
     * model, this re-creates the translator from the **cached** model — no network needed, nearly
     * instant.
     */
    suspend fun initIfReady() {
        // Reset status to prevent restart loop — the singleton survives
        // activity recreation, so Restarting would re-trigger the auto-restart
        _status.value = TranslatorStatus.Idle

        // Already initialized
        if (_translator.value != null) {
            Log.d(TAG, "initIfReady: already initialized, skipping")
            return
        }

        val user = Repository.getUser() ?: return
        if (!user.translatorReady || user.language.isNullOrEmpty()) {
            Log.d(
                    TAG,
                    "initIfReady: user not ready (translatorReady=${user.translatorReady}, language=${user.language})"
            )
            return
        }

        // English → English: no download needed
        if (user.language == TranslateLanguage.ENGLISH) {
            Log.d(TAG, "initIfReady: English — creating passthrough translator")
            val translator =
                    Utils.prepareTranslator(
                            sourceLanguage = TranslateLanguage.ENGLISH,
                            targetLanguage = TranslateLanguage.ENGLISH
                    )
            _translator.value = translator
            Log.d(TAG, "initIfReady: translator restored from cache ✅")
            return
        }

        try {
            Log.d(TAG, "initIfReady: restoring translator for ${user.language}")
            val translator = Repository.prepareTranslator()
            _translator.value = translator
            Log.d(TAG, "initIfReady: translator restored from cache ✅")
        } catch (e: Exception) {
            Log.e(TAG, "initIfReady: failed to restore translator", e)
            // Don't crash — the app works without translation, just untranslated
        }
    }

    /**
     * Translate a string, returning the original if translator is null or translation fails.
     * Convenience function for use anywhere.
     */
    suspend fun translate(text: String): String {
        val t = _translator.value ?: return text
        return try {
            t.translate(text).await()
        } catch (e: Exception) {
            Log.e(TAG, "translate failed for '$text'", e)
            text
        }
    }

    /** Reset state (e.g. when user changes language). */
    fun reset() {
        _translator.value = null
        _pendingLanguage.value = null
        _status.value = TranslatorStatus.Idle
    }

    /**
     * Called when the user chooses "Continue without translation" while offline. Saves the language
     * choice and registers a retry task so the download resumes automatically when internet
     * returns.
     */
    fun skipForNow(language: String) {
        _pendingLanguage.value = language
        _status.value = TranslatorStatus.Idle
        App.ioScope.launch {
            NetworkRecoveryManager.addPendingTask(
                    NetworkRecoveryManager.PendingTask(id = TASK_ID, label = "Translation model") {
                        prepare(language)
                    }
            )
        }
        Log.d(TAG, "Skipped download for '$language' — will retry when online")
    }

    /** Check if an exception is network-related. */
    private fun isNetworkError(e: Exception): Boolean {
        return e is IOException ||
                e is UnknownHostException ||
                e.cause is IOException ||
                e.cause is UnknownHostException
    }
}

enum class TranslatorStatus {
    Idle,
    Saving, // "Saving language..."
    Downloading, // "Downloading language model..."
    SlowDownload, // "Still downloading, please stay connected..."
    InternetLost, // "Internet lost. Download interrupted."
    Failed, // "Download failed."
    Restarting // "Applying language, restarting..."
}
