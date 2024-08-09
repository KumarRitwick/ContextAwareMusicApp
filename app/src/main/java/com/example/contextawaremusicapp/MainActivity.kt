package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.controller.AuthActivity
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.PlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.UserResponse
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var playbackBar: View
    private lateinit var currentTrackName: TextView
    private lateinit var playPauseButton: ImageButton
    private var isPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTokenValid()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        setupUI()
        setupNavigation()
    }

    private fun setupUI() {
        playbackBar = findViewById(R.id.playback_bar)
        currentTrackName = findViewById(R.id.current_track_name)
        playPauseButton = findViewById(R.id.play_pause_button)

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseTrack()
            } else {
                resumeTrack()
            }
        }

        playbackBar.setOnClickListener {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController
            navController?.navigate(R.id.navigation_player_screen)
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    fun playPlaylist(playlistUri: String) {
        Log.d("MainActivity", "Attempting to play playlist: $playlistUri")
        lifecycleScope.launch {
            try {
                SpotifyRemoteManager.playTrack(playlistUri)
                isPlaying = true
                updatePlaybackBar("Playing playlist...", isPlaying)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error playing playlist: $playlistUri", e)
            }
        }
    }

    private fun pauseTrack() {
        lifecycleScope.launch {
            try {
                SpotifyRemoteManager.pausePlayback()
                isPlaying = false
                updatePlaybackBar(currentTrackName.text.toString(), isPlaying)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error pausing track", e)
            }
        }
    }

    private fun resumeTrack() {
        lifecycleScope.launch {
            try {
                SpotifyRemoteManager.resumePlayback()
                isPlaying = true
                updatePlaybackBar(currentTrackName.text.toString(), isPlaying)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error resuming track", e)
            }
        }
    }

    private fun updatePlaybackBar(trackName: String, isPlaying: Boolean) {
        currentTrackName.text = trackName
        playPauseButton.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        playbackBar.visibility = View.VISIBLE
    }

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

            // Observe track changes
            SpotifyRemoteManager.observeTrackChanges { trackName ->
                updatePlaybackBar(trackName, isPlaying = true)
            }
        }, { throwable ->
            Log.e("MainActivity", "Failed to connect to Spotify App Remote", throwable)
        })
    }

    fun playAudiobook(audiobookUri: String) {
        Log.d("MainActivity", "Attempting to play audiobook: $audiobookUri")
        lifecycleScope.launch {
            try {
                SpotifyRemoteManager.playTrack(audiobookUri)
                isPlaying = true
                updatePlaybackBar("Playing audiobook...", isPlaying)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error playing audiobook: $audiobookUri", e)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyRemoteManager.disconnect()
    }
}
