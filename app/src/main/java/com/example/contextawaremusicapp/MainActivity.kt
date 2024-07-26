package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.controller.AuthActivity
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTokenValid()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        // Setup Bottom Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun getAccessToken(context: Context): String {
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

    private fun isTokenValid(): Boolean {
        val accessToken = getAccessToken(this)
        val expiryTime = getExpiryTime(this)
        return accessToken.isNotEmpty() && System.currentTimeMillis() < expiryTime
    }

    private fun getExpiryTime(context: Context): Long {
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

        return sharedPreferences.getLong("EXPIRY_TIME", 0L)
    }

    override fun onStart() {
        super.onStart()
        SpotifyRemoteManager.connect(this, {
            Log.d("MainActivity", "Connected to Spotify App Remote")
        }, { throwable ->
            Log.e("MainActivity", "Failed to connect to Spotify App Remote", throwable)
        })
    }

    override fun onStop() {
        super.onStop()
        SpotifyRemoteManager.disconnect()
    }
}
