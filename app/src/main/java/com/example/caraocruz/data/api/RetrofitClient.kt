package com.example.caraocruz.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // REEMPLAZAR con la URL real de tus Firebase Functions
    private const val BASE_URL = "http://127.0.0.1:5001/cara-o-cruz-91aab/us-central1/procesarApuesta"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val instance: JuegoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JuegoApiService::class.java)
    }
}
