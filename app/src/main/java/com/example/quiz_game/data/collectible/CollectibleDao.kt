package com.example.quiz_game.data.collectible

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CollectibleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg collectible: Collectible)

    @Query("SELECT * FROM collectibles") // use LIMIT when data scales largely
    fun get(): List<Collectible>

    @Query("SELECT * FROM collectibles WHERE uid = :uid")
    fun getByUid(uid: String): Collectible?

    @Query("DELETE FROM collectibles")
    fun delete()

    @Query("DELETE FROM collectibles WHERE uid = :uid")
    fun deleteByUid(uid: String)

    @Query("UPDATE collectibles SET acquiredAt = :acquiredAt WHERE uid = :uid")
    fun updateAcquiredAt(uid: String, acquiredAt: String)
}