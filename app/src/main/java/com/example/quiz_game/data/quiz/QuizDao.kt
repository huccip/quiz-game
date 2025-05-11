package com.example.quiz_game.data.quiz

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg quiz: Quiz)

    @Query("SELECT * FROM quizzes")
    fun get(): List<Quiz>

    @Query("SELECT * FROM quizzes WHERE category = :category")
    fun getByCategory(category: String): List<Quiz>

    @Query("SELECT * FROM quizzes WHERE uid = :uid")
    fun getByUid(uid: String): Quiz?

    @Query("SELECT * FROM quizzes WHERE uid IN (:uids)")
    fun getBySession(uids: List<String>): List<Quiz>

    @Query("DELETE FROM quizzes WHERE uid = :uid")
    fun deleteByUid(uid: String)

    @Query("DELETE FROM quizzes")
    fun truncate()

    @Query("UPDATE quizzes SET expired = 1 WHERE uid = :uid")
    fun updateExpired(uid: String)
}