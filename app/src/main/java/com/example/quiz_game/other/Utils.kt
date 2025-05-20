package com.example.quiz_game.other

import android.content.res.Configuration
import android.text.Html
import android.util.Log
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.example.quiz_game.data.collectible.Collectible
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.io.InputStream
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
        onFinish: suspend () -> Unit,
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

    suspend inline fun <reified T> readJsonRaw(
        inputStream: InputStream,
        onFinish: suspend (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        return try {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            onFinish(Json.decodeFromString(jsonString))
        } catch (e: Exception) {
            onError(e)
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

    

    fun stringDateFormat(date: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateObject = Date(date)
        return sdf.format(dateObject)
    }

    fun achievementIcon(achievement: Int): Pair<Int, Int> {
        return when (achievement) {
            R.string.achievements_no_mistakes -> {
                R.drawable.ic_no_mistake to R.string.achievements_detailed_no_mistakes
            }

            R.string.achievements_new_record -> {
                R.drawable.ic_new_record to R.string.achievements_detailed_new_record
            }

            R.string.achievements_one_mistake -> {
                R.drawable.ic_one_mistake to R.string.achievements_detailed_one_mistake
            }

            R.string.achievements_two_mistakes -> {
                R.drawable.ic_two_mistake to R.string.achievements_detailed_two_mistakes
            }

            R.string.achievements_ten_mistakes -> {
                R.drawable.ic_ten_mistake to R.string.achievements_detailed_ten_mistakes
            }

            R.string.achievements_twenty_mistakes -> {
                R.drawable.ic_twenty_mistakes to R.string.achievements_detailed_twenty_mistakes
            }

            // TODO: find corresponding icons
            R.string.achievement_timelapse_quick_thinker -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_quick_thinker
            }

            R.string.achievement_timelapse_casual_cruiser -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_casual_cruiser
            }

            R.string.achievement_timelapse_steady_strategist -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_steady_strategist
            }

            R.string.achievement_timelapse_brain_marathoner -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_brain_marathoner
            }

            R.string.achievement_timelapse_quiz_zen_master -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_quiz_zen_master
            }

            R.string.achievement_timelapse_time_bender -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_time_bender
            }

            R.string.achievement_timelapse_eternal_quizzer -> {
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_eternal_quizzer
            }

            else -> {
                R.drawable.ic_launcher_foreground to R.string.achievements_empty
            }
        }
    }

    fun decodeHtml(html: String): String =
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

}