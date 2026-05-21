package com.example.caraocruz.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    // URLs para producción
    private const val BASE_URL_APUESTA_PROD = "https://procesarapuesta-36bckviauq-uc.a.run.app/"
    private const val BASE_URL_RANKING_PROD = "https://getranking-36bckviauq-uc.a.run.app/"
    
    // URL para desarrollo (Firebase Emulator)
    private const val BASE_URL_DEV = "http://10.0.2.2:5001/cara-o-cruz-91aab/us-central1/"

    // Cambia esto a false para usar la URL de producción
    private const val USE_EMULATOR = false

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val instance: JuegoApiService by lazy {
        val url = if (USE_EMULATOR) BASE_URL_DEV else BASE_URL_APUESTA_PROD
        createRetrofit(url).create(JuegoApiService::class.java)
    }

    val rankingInstance: JuegoApiService by lazy {
        val url = if (USE_EMULATOR) BASE_URL_DEV else BASE_URL_RANKING_PROD
        createRetrofit(url).create(JuegoApiService::class.java)
    }
}
