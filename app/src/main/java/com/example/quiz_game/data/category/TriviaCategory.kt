package com.example.quiz_game.data.category

import com.google.gson.annotations.SerializedName

data class TriviaCategory(
    @SerializedName("trivia_categories")
    val triviaCategories: List<Category>
)
