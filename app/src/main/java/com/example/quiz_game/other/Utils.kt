package com.example.quiz_game.other

import android.content.Context
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Html
import com.example.quiz_game.App
import com.example.quiz_game.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    private const val TAG = "Utils"

    inline fun <reified T> readJsonRaw(inputStream: InputStream): T {
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        return Json.decodeFromString(jsonString)
    }

    suspend fun prepareTranslator(
        sourceLanguage: String,
        targetLanguage: String,
    ): Translator {
        val options =
            TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()

        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()

        return translator
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

    fun categoryImageRes(categoryId: Int?): Int {
        return when (categoryId) {
            9 -> R.drawable.img_category_general
            10 -> R.drawable.img_category_books
            11 -> R.drawable.img_category_movies
            12 -> R.drawable.img_category_music
            13 -> R.drawable.img_category_musicals
            14 -> R.drawable.img_category_series
            15 -> R.drawable.img_category_video_games
            16 -> R.drawable.img_category_board_games
            17 -> R.drawable.img_category_nature
            18 -> R.drawable.img_category_computers
            19 -> R.drawable.img_category_math
            20 -> R.drawable.img_category_mythology
            21 -> R.drawable.img_category_sports
            22 -> R.drawable.img_category_geography
            23 -> R.drawable.img_category_history
            24 -> R.drawable.img_category_politics
            25 -> R.drawable.img_category_history
            26 -> R.drawable.img_category_movies
            27 -> R.drawable.img_category_animals
            28 -> R.drawable.img_category_general
            29 -> R.drawable.img_category_books
            30 -> R.drawable.img_category_computers
            31 -> R.drawable.img_category_movies
            32 -> R.drawable.img_category_movies
            else -> R.drawable.img_category_general
        }
    }

    fun achievementIcon(achievement: Int): Pair<Int, Int> {
        return when (achievement) {
            R.string.achievements_new_record ->
                R.drawable.ic_new_record to R.string.achievements_detailed_new_record

            R.string.achievements_no_mistakes ->
                R.drawable.ic_no_mistake to R.string.achievements_detailed_no_mistakes

            R.string.achievements_one_mistake ->
                R.drawable.ic_one_mistake to R.string.achievements_detailed_one_mistake

            R.string.achievements_two_mistakes ->
                R.drawable.ic_two_mistake to R.string.achievements_detailed_two_mistakes

            R.string.achievements_rough_session ->
                R.drawable.ic_frown to R.string.achievements_detailed_rough_session

            R.string.achievements_first_session ->
                R.drawable.ic_arrow_forward to R.string.achievements_detailed_first_session

            R.string.achievement_timelapse_quick_thinker ->
                R.drawable.ic_fire to R.string.achievement_timelapse_detailed_quick_thinker

            R.string.achievement_timelapse_casual_cruiser ->
                R.drawable.ic_feather to R.string.achievement_timelapse_detailed_casual_cruiser

            R.string.achievement_timelapse_brain_marathoner ->
                R.drawable.ic_arrow_north_east to R.string.achievement_timelapse_detailed_brain_marathoner

            R.string.achievement_timelapse_eternal_quizzer ->
                R.drawable.ic_wave to R.string.achievement_timelapse_detailed_eternal_quizzer

            else ->
                R.drawable.ic_launcher_foreground to R.string.achievements_empty
        }
    }

    fun decodeHtml(html: String): String =
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

    suspend fun greetingBasedOnTimezone(translator: Translator? = null): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val message =
            when (currentHour) {
                in 0..11 -> "Good morning"
                in 12..17 -> "Good afternoon"
                else -> "Good evening"
            }

        return translator?.translate(message)?.await() ?: message
    }

    fun hasWifiOn(): Boolean {
        val connectivityManager =
            App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun hasInternet(): Boolean {
        val connectivityManager =
            App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun checkIsMidnight() : Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return currentHour == 0
    }
}
