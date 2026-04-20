package com.example.quiz_game.data.quote

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val quote: String? = null,
    val author: String? = null,
    val dateOfRetrieval: Long = System.currentTimeMillis()
) {
    companion object {
        const val KEY_QUOTE = "quote"
    }
}
