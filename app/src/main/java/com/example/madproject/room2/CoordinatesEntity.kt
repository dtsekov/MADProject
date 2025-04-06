package com.example.madproject.room2

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "coordinates")
data class CoordinatesEntity(
    @PrimaryKey val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
