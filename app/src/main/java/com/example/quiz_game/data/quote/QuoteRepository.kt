package com.example.quiz_game.data.quote

import com.example.quiz_game.data.Service

object QuoteRepository {

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

    suspend fun report(type: ReportType) {
        // TODO: update report function
    }
}
