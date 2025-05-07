package com.example.quiz_game.data

import com.example.quiz_game.data.category.TriviaCategoryService
import com.example.quiz_game.data.quiz.TriviaQuizService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val TRIVIA = "https://opentdb.com/"

object Service {

    private val retrofit = Retrofit.Builder()
        .baseUrl(TRIVIA)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quizService: TriviaQuizService by lazy { retrofit.create(TriviaQuizService::class.java) }
    val categoryService: TriviaCategoryService by lazy { retrofit.create(TriviaCategoryService::class.java) }
}