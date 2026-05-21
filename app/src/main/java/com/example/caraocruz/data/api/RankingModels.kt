package com.example.caraocruz.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RankingItem(
    @Json(name = "nombreUsuario") val nombreUsuario: String,
    @Json(name = "premio") val premio: Long,
    @Json(name = "fecha") val fecha: Long // Timestamp as Long
)
