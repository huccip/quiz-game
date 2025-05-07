package com.example.quiz_game.other

import android.content.Context
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale
import java.util.concurrent.TimeUnit

typealias Minutes = Long

object Utils {

    private const val TAG = "test1234 Utils"

    suspend fun runWithTimeout(
        block: suspend () -> Unit,
        timeout: Minutes = 2,
        onFinish: () -> Unit,
        onTimeout: (Throwable) -> Unit
    ) {
        try {
            withTimeout(TimeUnit.MINUTES.toMillis(timeout)) {
                withContext(Dispatchers.IO) {
                    block()
                    onFinish()
                }
            }
        } catch (e: TimeoutCancellationException) {
            onTimeout(e)
        }
    }

    fun prepareTranslator(
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (Translator) -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val options = TranslatorOptions
            .Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded(
            DownloadConditions.Builder()
                .requireWifi()
                .build()
        )
            .addOnFailureListener {
                Log.e(TAG, "prepareTranslator: translation model download failed ⛔", it)
                onError(it)
            }
            .addOnSuccessListener {
                Log.e(TAG, "prepareTranslator: translation model downloaded 🎯")
                onSuccess(translator)
            }
    }

    fun supportedLanguage(language: String): Boolean {
        val supportedLanguages = Constants.SUPPORTED_LANGUAGES.map { it.first.lowercase() }

        return supportedLanguages.contains(language.lowercase())
    }

    fun countryFlag(countryCode: String = "US", type: String = "shiny", size: Int = 64): String {
        val supportedCountries = Constants.SUPPORTED_LANGUAGES.map { it.third.lowercase() }

        return "https://flagsapi.com/${
            if (supportedCountries.contains(countryCode.lowercase())) countryCode
            else "US"
        }/$type/$size.png"
    }

    fun updateAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        context.applicationContext.createConfigurationContext(config)
    }
}