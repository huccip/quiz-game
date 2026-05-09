package com.example.quiz_game.data.category

import retrofit2.Response
import retrofit2.http.GET

private const val ENDPOINT = "api_category.php"

interface TriviaCategoryService {
    @GET(ENDPOINT)
    suspend fun get(): Response<TriviaCategory>
}
