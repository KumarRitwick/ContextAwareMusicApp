package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.controller.AuthActivity
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var currentlyPlayingImage: ImageView
    private lateinit var currentlyPlayingTitle: TextView
    private lateinit var currentlyPlayingArtist: TextView
    private lateinit var playPauseButton: ImageButton

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
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // Initialize Currently Playing Bar
        val currentlyPlayingBar = LayoutInflater.from(this).inflate(R.layout.currently_playing_bar, findViewById(R.id.currently_playing_bar), true)
        currentlyPlayingImage = currentlyPlayingBar.findViewById(R.id.currently_playing_image)
        currentlyPlayingTitle = currentlyPlayingBar.findViewById(R.id.currently_playing_title)
        currentlyPlayingArtist = currentlyPlayingBar.findViewById(R.id.currently_playing_artist)
        playPauseButton = currentlyPlayingBar.findViewById(R.id.play_pause_button)

        playPauseButton.setOnClickListener {
            SpotifyRemoteManager.togglePlayPause({
                updatePlayPauseButton()
            }, { error ->
                Toast.makeText(this, "Error toggling play/pause: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }

        updateCurrentlyPlayingTrack()
    }

    fun updateCurrentlyPlayingTrack() {
        SpotifyRemoteManager.getPlayerState { playerState ->
            val track = playerState.track
            currentlyPlayingTitle.text = track.name
            currentlyPlayingArtist.text = track.artist.name
            Glide.with(this)
                .load(track.imageUri.raw)
                .into(currentlyPlayingImage)
            updatePlayPauseButton()
        }
    }

    private fun updatePlayPauseButton() {
        SpotifyRemoteManager.getPlayerState { playerState ->
            val isPlaying = !playerState.isPaused
            playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                )
            )
        }
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
