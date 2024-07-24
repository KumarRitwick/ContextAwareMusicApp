package com.example.contextawaremusicapp.controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.example.contextawaremusicapp.MainActivity

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AuthActivity", "onCreate called")
        startAuthorization()
    }

    private fun startAuthorization() {
        val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf("user-read-private", "playlist-read-private", "playlist-read-collaborative", "streaming"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        Log.d("AuthActivity", "onActivityResult called")

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            Log.d("AuthActivity", "Authorization response received: ${response.type}")
            if (response.type == AuthorizationResponse.Type.TOKEN) {
                val accessToken = response.accessToken
                val expiresIn = response.expiresIn
                Log.d("AuthActivity", "Access Token Received: $accessToken")

                saveAccessToken(this, accessToken, expiresIn)
                val returnIntent = Intent(this, MainActivity::class.java)
                startActivity(returnIntent)
                finish()
            } else if (response.type == AuthorizationResponse.Type.ERROR) {
                Log.e("AuthActivity", "Failed to receive token: ${response.error}")
                Toast.makeText(this, "Authentication failed: ${response.error}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("AuthActivity", "Request code did not match")
        }
    }

    private fun saveAccessToken(context: Context, token: String, expiresIn: Int) {
        Log.d("AuthActivity", "Storing Access Token: $token")
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

        val expiryTime = System.currentTimeMillis() + expiresIn * 1000
        sharedPreferences.edit()
            .putString("ACCESS_TOKEN", token)
            .putLong("EXPIRY_TIME", expiryTime)
            .apply()
        Log.d("AuthActivity", "Access Token Stored: ${sharedPreferences.getString("ACCESS_TOKEN", "")}")
    }

    companion object {
        const val CLIENT_ID = "cffc2e76239543e18c44e888d655a3a3"
        const val REDIRECT_URI = "contextawaremusicapp://callback"
        const val AUTH_TOKEN_REQUEST_CODE = 0x10
    }
}
