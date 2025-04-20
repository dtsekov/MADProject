package com.example.madproject.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
@Dao
interface ICoordinatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coordinates: List<CoordinatesEntity>)
    @Insert
    suspend fun insert(coordinates: CoordinatesEntity)
    @Query("SELECT * FROM coordinates")
    suspend fun getAll(): List<CoordinatesEntity>
    @Query("SELECT COUNT(*) FROM coordinates")
    fun getCount(): Int

}
