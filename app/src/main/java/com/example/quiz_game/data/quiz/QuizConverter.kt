package com.example.quiz_game.data.quiz

import androidx.compose.ui.util.fastJoinToString
import androidx.room.TypeConverter

class IncorrectAnswersConverter {
    @TypeConverter
    fun toString(list: List<String>): String = list.fastJoinToString("\n")

    @TypeConverter
    fun fromString(str: String): List<String> = str.split("\n")
}