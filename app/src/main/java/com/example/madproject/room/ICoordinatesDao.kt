package com.example.madproject.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
@Dao
interface ICoordinatesDao {
    @Insert
    suspend fun insert(coordinates: CoordinatesEntity)
    @Query("SELECT * FROM coordinates")
    suspend fun getAll(): List<CoordinatesEntity>
    @Query("SELECT COUNT(*) FROM coordinates")
    fun getCount(): Int
    @Query("DELETE FROM coordinates WHERE code = :code")
    fun deleteWithTimestamp(code: String)
    @Update
    suspend fun updateCoordinate(coordinates: CoordinatesEntity)
    @Query("SELECT * FROM coordinates WHERE code = :code LIMIT 1")
    suspend fun getCoordinateByTimestamp(code: String): CoordinatesEntity?
}
