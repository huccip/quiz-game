package com.example.quiz_game.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg session: Session)

    @Query("SELECT * FROM sessions")
    fun get(): List<Session>

    @Query("SELECT * FROM sessions WHERE uid = :uid")
    fun getByUid(uid: String): Session?

    @Query("DELETE FROM sessions")
    fun truncate()

    @Query("DELETE FROM sessions WHERE uid = :uid")
    fun deleteByUid(uid: String)

    @Query("UPDATE sessions SET quizzesUids = :quizzesUids WHERE uid = :uid")
    fun updateQuizzesUids(uid: String, quizzesUids: List<String>)

    @Query("UPDATE sessions SET score = :score WHERE uid = :uid")
    fun updateScore(uid: String, score: Int)

    @Query("UPDATE sessions SET maxScore = :maxScore WHERE uid = :uid")
    fun updateMaxScore(uid: String, maxScore: Int)

    @Query("UPDATE sessions SET achievements = :achievements WHERE uid = :uid")
    fun updateAchievements(uid: String, achievements: List<Int>)

    @Query("UPDATE sessions SET createdAt = :startedAt WHERE uid = :uid")
    fun updateCreatedAt(uid: String, startedAt: Long)

    @Query("UPDATE sessions SET expiredAt = :finishedAt WHERE uid = :uid")
    fun updateExpiredAt(uid: String, finishedAt: Long)
}