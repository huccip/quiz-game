package com.example.quiz_game.data.quiz

import com.google.gson.annotations.SerializedName

data class TriviaQuiz(
    @SerializedName("response_code")
    val responseCode: Int,
    val results: List<Quiz>
) {
    fun mapResponseCodeToReadableInfo(): String {
        return when (responseCode) {
            0 -> "Success: Returned results successfully."
            1 -> "No Results: The API doesn't have enough questions for your query."
            2 -> "Invalid Parameter: One or more arguments passed in aren't valid."
            3 -> "Token Not Found: Session Token does not exist."
            4 -> "Token Empty: All possible questions have been returned. Reset the token."
            5 -> "Rate Limit: Too many requests. Please wait before trying again."
            else -> "Unknown Response Code: Something unexpected happened."
        }
    }
}