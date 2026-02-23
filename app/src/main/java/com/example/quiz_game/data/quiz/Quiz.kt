package com.example.quiz_game.data.quiz

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "quizzes")
data class Quiz(
        @PrimaryKey(autoGenerate = false) var uid: String = "",
        var type: String? = null,
        var difficulty: String? = null,
        var category: String? = null,
        var categoryUid: String? = null,
        var question: String? = null,
        @SerializedName("correct_answer") var correctAnswer: String? = null,
        @SerializedName("incorrect_answers") var incorrectAnswers: List<String>? = null,
        var mark: Int? = null,
        var expired: Boolean = false,
) {
    fun generateUid(): String =
            Base64.encodeToString(
                    ("${category ?: ""}${question ?: ""}${correctAnswer ?: ""}").toByteArray(),
                    Base64.NO_WRAP
            )

    fun generateMark(): Int =
            when (difficulty) {
                "easy" -> 1
                "medium" -> 2
                else -> 3 // hard
            }
}
