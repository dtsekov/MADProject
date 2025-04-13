package com.example.madproject.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types

// Data models for the OSRM response
@JsonClass(generateAdapter = true)
data class OSRMResponse(
    val routes: List<Route>
)

@JsonClass(generateAdapter = true)
data class Route(
    val geometry: Geometry
)

@JsonClass(generateAdapter = true)
data class Geometry(
    val coordinates: List<List<Double>>
)