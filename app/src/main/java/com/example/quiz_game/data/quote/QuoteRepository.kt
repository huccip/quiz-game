package com.example.quiz_game.data.quote

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.quiz_game.data.Service
import androidx.core.net.toUri
import com.example.quiz_game.data.Repository
import com.example.quiz_game.other.Utils

object QuoteRepository {

    private const val REPORT_EMAIL = "quiz-app.report@yopmail.com"

    enum class ReportType {
        Nudity,
        Violence,
        Racism,
        SomethingElse
    }

    suspend fun get(): Quote {
        var quote = Repository.getQuote()

        if (quote != null && !Utils.checkHasADayPassedSince(quote.dateOfRetrieval)) return quote

        val response = Service.quoteService.get()

        if (!response.isSuccessful) {
            throw Exception("Error while getting quote: ${response.code()}")
        }

        quote = response.body() ?: throw Exception("Error while getting quote: Empty body")

        Repository.saveQuote(quote)

        return quote
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun report(context: Context, type: ReportType) {
        val intent =
                Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(REPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "Quote Report: ${type.name}")
                    putExtra(
                            Intent.EXTRA_TEXT,
                            "I would like to report this quote for the following reason: ${type.name}"
                    )
                }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
