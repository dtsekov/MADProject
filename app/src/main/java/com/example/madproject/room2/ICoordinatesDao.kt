package com.example.madproject.room2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ICoordinatesDao {
    @Insert
    suspend fun insert(coordinates: CoordinatesEntity2)
    @Query("SELECT * FROM coordinates")
    suspend fun getAll(): List<CoordinatesEntity2>
    @Query("SELECT COUNT(*) FROM coordinates")
    fun getCount(): Int
    @Query("DELETE FROM coordinates WHERE timestamp = :timestamp")
    fun deleteWithTimestamp(timestamp: Long)
    @Update
    suspend fun updateCoordinate(coordinates: CoordinatesEntity2)
    @Query("SELECT * FROM coordinates WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getCoordinateByTimestamp(timestamp: Long): CoordinatesEntity2?
}
