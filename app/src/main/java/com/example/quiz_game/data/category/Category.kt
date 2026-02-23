package com.example.quiz_game.data.category

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("categories")
data class Category(
        @PrimaryKey(autoGenerate = false) var uid: String = "",
        var id: Int? = null,
        var name: String? = null,
) {
    fun generateUid(): String =
            Base64.encodeToString(("${id ?: 0}${name ?: ""}").toByteArray(), Base64.NO_WRAP)
}
