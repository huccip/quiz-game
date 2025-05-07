package com.example.quiz_game.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quiz_game.data.category.Category
import com.example.quiz_game.data.category.CategoryDao
import com.example.quiz_game.data.quiz.IncorrectAnswersConverter
import com.example.quiz_game.data.quiz.Quiz
import com.example.quiz_game.data.quiz.QuizDao

@TypeConverters(IncorrectAnswersConverter::class)
@Database(
    entities = [Quiz::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun categoryDao(): CategoryDao
}