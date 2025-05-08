package com.example.quiz_game.other

import android.content.Context
import android.content.res.Configuration
import android.text.Html
import android.util.Log
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
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

    fun updateAppLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = App.instance.baseContext.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics) // ✅ force update
    }

    fun stringDateFormat(date: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateObject = Date(date)
        return sdf.format(dateObject)
    }

    fun achievementIcon(achievement: String, context: Context): Int {
        return when (achievement) {
            context.getString(R.string.achievements_no_mistakes) -> {
                R.drawable.ic_no_mistake
            }

            context.getString(R.string.achievements_new_record) -> {
                R.drawable.ic_new_record
            }

            context.getString(R.string.achievements_one_mistake) -> {
                R.drawable.ic_one_mistake
            }

            context.getString(R.string.achievements_two_mistakes) -> {
                R.drawable.ic_two_mistake
            }

            context.getString(R.string.achievements_ten_mistakes) -> {
                R.drawable.ic_ten_mistake
            }

            context.getString(R.string.achievements_twenty_mistakes) -> {
                R.drawable.ic_twenty_mistakes
            }

            else -> {
                R.drawable.ic_launcher_foreground
            }
        }
    }

    fun decodeHtml(html: String): String =
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

}