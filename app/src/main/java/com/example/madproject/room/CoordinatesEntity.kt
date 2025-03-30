package com.example.madproject.room

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "coordinates")
data class CoordinatesEntity(
    @PrimaryKey val code: String,
    val latitude: Double,
    val longitude: Double
)
