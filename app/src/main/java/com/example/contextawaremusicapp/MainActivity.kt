package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
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
import com.example.contextawaremusicapp.model.SpotifyImage
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var currentlyPlayingImage: ImageView
    private lateinit var currentlyPlayingTitle: TextView
    private lateinit var currentlyPlayingArtist: TextView
    private lateinit var playPauseButton: ImageButton

    private lateinit var fullScreenPlayerImage: ImageView
    private lateinit var fullScreenPlayerTitle: TextView
    private lateinit var fullScreenPlayerArtist: TextView
    private lateinit var playPauseButtonFull: ImageButton
    private lateinit var prevButtonFull: ImageButton
    private lateinit var nextButtonFull: ImageButton
    private lateinit var fullScreenPlayerContainer: FrameLayout

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
        val currentlyPlayingBar = findViewById<FrameLayout>(R.id.currently_playing_bar_container)
        val currentlyPlayingBarView = LayoutInflater.from(this).inflate(R.layout.currently_playing_bar, currentlyPlayingBar, true)
        currentlyPlayingImage = currentlyPlayingBarView.findViewById(R.id.currently_playing_image)
        currentlyPlayingTitle = currentlyPlayingBarView.findViewById(R.id.currently_playing_title)
        currentlyPlayingArtist = currentlyPlayingBarView.findViewById(R.id.currently_playing_artist)
        playPauseButton = currentlyPlayingBarView.findViewById(R.id.play_pause_button)

        playPauseButton.setOnClickListener {
            SpotifyRemoteManager.togglePlayPause({
                updatePlayPauseButton()
            }, { error ->
                Toast.makeText(this, "Error toggling play/pause: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }

        currentlyPlayingBarView.setOnClickListener {
            expandFullScreenPlayer()
        }

        // Initialize Full-Screen Player
        fullScreenPlayerContainer = findViewById(R.id.full_screen_player_container)
        val fullScreenPlayer = LayoutInflater.from(this).inflate(R.layout.full_screen_player, fullScreenPlayerContainer, true)
        fullScreenPlayerImage = fullScreenPlayer.findViewById(R.id.full_screen_player_image)
        fullScreenPlayerTitle = fullScreenPlayer.findViewById(R.id.full_screen_player_title)
        fullScreenPlayerArtist = fullScreenPlayer.findViewById(R.id.full_screen_player_artist)
        playPauseButtonFull = fullScreenPlayer.findViewById(R.id.play_pause_button_full)
        prevButtonFull = fullScreenPlayer.findViewById(R.id.prev_button)
        nextButtonFull = fullScreenPlayer.findViewById(R.id.next_button)

        playPauseButtonFull.setOnClickListener {
            SpotifyRemoteManager.togglePlayPause({
                updatePlayPauseButtonFull()
            }, { error ->
                Toast.makeText(this, "Error toggling play/pause: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }

        prevButtonFull.setOnClickListener {
            SpotifyRemoteManager.skipToPrevious({
                updateCurrentlyPlayingTrack()
            }, { error ->
                Toast.makeText(this, "Error skipping to previous: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }

        nextButtonFull.setOnClickListener {
            SpotifyRemoteManager.skipToNext({
                updateCurrentlyPlayingTrack()
            }, { error ->
                Toast.makeText(this, "Error skipping to next: ${error.message}", Toast.LENGTH_SHORT).show()
            })
        }

        fullScreenPlayerContainer.setOnClickListener {
            collapseFullScreenPlayer()
        }

        updateCurrentlyPlayingTrack()
    }

    // Function to update the currently playing track details
    fun updateCurrentlyPlayingTrack() {
        SpotifyRemoteManager.getPlayerState({ playerState ->
            val track = playerState.track
            currentlyPlayingTitle.text = track.name
            currentlyPlayingArtist.text = track.artist.name
            Glide.with(this)
                .load(track.imageUri.raw?.let { SpotifyImage(it) })
                .into(currentlyPlayingImage)
            updatePlayPauseButton()

            fullScreenPlayerTitle.text = track.name
            fullScreenPlayerArtist.text = track.artist.name
            Glide.with(this)
                .load(track.imageUri.raw?.let { SpotifyImage(it) })
                .into(fullScreenPlayerImage)
            updatePlayPauseButtonFull()
        }, { error ->
            Toast.makeText(this, "Error retrieving player state: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    // Function to update the play/pause button
    private fun updatePlayPauseButton() {
        SpotifyRemoteManager.getPlayerState({ playerState ->
            val isPlaying = !playerState.isPaused
            playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                )
            )
        }, { error ->
            Toast.makeText(this, "Error retrieving player state: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    // Function to update the play/pause button in fullscreen
    private fun updatePlayPauseButtonFull() {
        SpotifyRemoteManager.getPlayerState({ playerState ->
            val isPlaying = !playerState.isPaused
            playPauseButtonFull.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                )
            )
        }, { error ->
            Toast.makeText(this, "Error retrieving player state: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    // Function to expand the fullscreen player
    private fun expandFullScreenPlayer() {
        fullScreenPlayerContainer.visibility = View.VISIBLE
        fullScreenPlayerContainer.animate()
            .translationY(0f)
            .setDuration(300)
            .start()
    }

    // Function to collapse the fullscreen player
    private fun collapseFullScreenPlayer() {
        fullScreenPlayerContainer.animate()
            .translationY(fullScreenPlayerContainer.height.toFloat())
            .setDuration(300)
            .withEndAction {
                fullScreenPlayerContainer.visibility = View.GONE
            }
            .start()
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
