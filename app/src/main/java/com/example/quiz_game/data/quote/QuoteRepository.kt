package com.example.quiz_game.data.quote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.quiz_game.data.Service

object QuoteRepository {

    private const val REPORT_EMAIL = "quiz-app.report@yopmail.com"

    enum class ReportType {
        Nudity,
        Violence,
        Racism,
        SomethingElse
    }

    suspend fun get(): Quote {
        val response = Service.quoteService.get()
        if (!response.isSuccessful) {
            throw Exception("Error while getting quote: ${response.code()}")
        }
        return response.body() ?: throw Exception("Quote was not found")
    }

    fun report(context: Context, type: ReportType) {
        val intent =
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
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
