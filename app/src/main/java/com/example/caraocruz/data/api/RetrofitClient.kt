package com.example.caraocruz.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // URL para producción (Firebase Cloud Functions)
    private const val BASE_URL_PROD = "https://us-central1-cara-o-cruz-91aab.cloudfunctions.net/"
    
    // URL para desarrollo (Firebase Emulator)
    private const val BASE_URL_DEV = "http://10.0.2.2:5001/cara-o-cruz-91aab/us-central1/"

    // Cambia esto a false para usar la URL de producción
    private const val USE_EMULATOR = false

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val instance: JuegoApiService by lazy {
        val url = if (USE_EMULATOR) BASE_URL_DEV else BASE_URL_PROD
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JuegoApiService::class.java)
    }
}
