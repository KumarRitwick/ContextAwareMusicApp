package com.example.contextawaremusicapp.model

import android.content.Context
import android.util.Log
import com.example.contextawaremusicapp.controller.AuthActivity.Companion.CLIENT_ID
import com.example.contextawaremusicapp.controller.AuthActivity.Companion.CLIENT_SECRET
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
                var request = chain.request()
                val token = getAccessToken(appContext)
                Log.d("SpotifyApi", "Retrieved Access Token: $token")
                if (token.isEmpty()) {
                    throw IllegalStateException("Access token is missing")
                }
                request = request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = chain.proceed(request)
                if (response.code == 401) {
                    // Handle token refresh
                    val newToken = refreshToken(appContext)
                    if (newToken.isNotEmpty()) {
                        request = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newToken")
                            .build()
                        return@addInterceptor chain.proceed(request)
                    }
                }
                response
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

    private fun refreshToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SpotifyCredential", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("REFRESH_TOKEN", "") ?: ""
        if (refreshToken.isEmpty()) {
            return ""
        }

        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .addHeader("Authorization", "Basic " + android.util.Base64.encodeToString("$CLIENT_ID:$CLIENT_SECRET".toByteArray(), android.util.Base64.NO_WRAP))
            .post(
                okhttp3.FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", refreshToken)
                    .build()
            )
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = org.json.JSONObject(responseBody)
                    val newAccessToken = json.getString("access_token")
                    sharedPreferences.edit().putString("ACCESS_TOKEN", newAccessToken).apply()
                    return newAccessToken
                }
            }
        } catch (e: Exception) {
            Log.e("SpotifyApi", "Error refreshing token", e)
        }

        return ""
    }
}
