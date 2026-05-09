package com.example.quiz_game.data.category

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg category: Category)

    @Query("SELECT * FROM categories")
    fun get(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getById(id: Int): Category?

    @Query("SELECT * FROM categories WHERE name = :name")
    fun getByName(name: String): Category?

    @Query("SELECT * FROM categories WHERE uid = :uid")
    fun getByUid(uid: String): Category?

    @Query("DELETE FROM categories")
    fun truncate()
}
