package com.example.contextawaremusicapp

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun getAccessToken(context: Context): String {
    Log.d("getAccessToken", "Attempting to retrieve access token")
    val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "SpotifyCredential",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val token = sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    Log.d("getAccessToken", "Retrieved Access Token: $token")
    return token
}
