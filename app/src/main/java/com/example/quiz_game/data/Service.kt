package com.example.quiz_game.data

import com.example.quiz_game.data.category.TriviaCategoryService
import com.example.quiz_game.data.quiz.TriviaQuizService
import com.example.quiz_game.data.quote.QuoteService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val TRIVIA = "https://opentdb.com/"
const val QUOTE = "https://quotes-api-self.vercel.app/"

object Service {

    private val retrofitTrivia = Retrofit.Builder()
        .baseUrl(TRIVIA)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitQuote = Retrofit.Builder()
        .baseUrl(QUOTE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quizService: TriviaQuizService by lazy { retrofitTrivia.create(TriviaQuizService::class.java) }
    val categoryService: TriviaCategoryService by lazy { retrofitTrivia.create(TriviaCategoryService::class.java) }
    val quoteService: QuoteService by lazy { retrofitQuote.create(QuoteService::class.java) }
}