package com.example.caraocruz.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface JuegoApiService {
    @POST("procesarApuesta")
    suspend fun enviarApuesta(
        @Header("Authorization") token: String,
        @Body request: ApuestaRequest
    ): ApuestaResponse
}
