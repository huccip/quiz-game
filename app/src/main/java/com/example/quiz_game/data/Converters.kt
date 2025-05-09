package com.example.quiz_game.data

import androidx.compose.ui.util.fastJoinToString
import androidx.room.TypeConverter

class ListStringConverter {
    @TypeConverter
    fun toString(list: List<String>): String = list.fastJoinToString("\n")

    @TypeConverter
    fun fromString(str: String): List<String> = str.split("\n")
}

class ListIntConverter {
    @TypeConverter
    fun toString(list: List<Int>): String = list.joinToString("\n")

    @TypeConverter
    fun fromString(str: String): List<Int> =
        if (str.isBlank()) emptyList()
        else str.split("\n").map { it.toInt() }
}
