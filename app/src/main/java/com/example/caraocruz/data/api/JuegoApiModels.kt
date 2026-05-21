package com.example.caraocruz.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApuestaRequest(
    @Json(name = "userId") val userId: String,
    @Json(name = "apuesta") val apuesta: Int,
    @Json(name = "eleccionCara") val eleccionCara: Boolean
)

@JsonClass(generateAdapter = true)
data class ApuestaResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "resultado") val resultado: String,
    @Json(name = "gano") val gano: Boolean,
    @Json(name = "nuevoSaldo") val nuevoSaldo: Long,
    @Json(name = "nuevoBote") val nuevoBote: Long,
    @Json(name = "premio") val premio: Long,
    @Json(name = "error") val error: String? = null
)
