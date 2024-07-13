package com.example.contextawaremusicapp.model

import android.content.Context
import android.util.Log
import com.example.contextawaremusicapp.getAccessToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpotifyApi {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private const val BASE_URL = "https://api.spotify.com/"

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val token = getAccessToken(appContext)
                Log.d("SpotifyApi", "Retrieved Access Token: $token")
                if (token.isEmpty()) {
                    throw IllegalStateException("Access token is missing")
                }
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: SpotifyService by lazy {
        retrofit.create(SpotifyService::class.java)
    }
}
