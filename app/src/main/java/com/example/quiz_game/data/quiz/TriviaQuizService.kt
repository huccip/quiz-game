package com.example.quiz_game.data.quiz

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

private const val ENDPOINT = "api.php"

interface TriviaQuizService {
    @GET(ENDPOINT)
    suspend fun get(
        @Query("amount") amount: Int,
        @Query("category") category: Int? = null,
    ): Response<TriviaQuiz>
}
