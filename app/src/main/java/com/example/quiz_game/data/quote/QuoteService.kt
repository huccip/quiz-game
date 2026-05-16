package com.example.quiz_game.data.quote

import retrofit2.Response
import retrofit2.http.GET

private const val ENDPOINT = "random"

interface QuoteService {
    @GET(ENDPOINT)
    suspend fun get(): Response<Quote>
}
