package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.controller.AuthActivity
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.PlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTokenValid()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PlaylistAdapter(listOf()) { playlistUri ->
            playPlaylist(playlistUri)
        }
        recyclerView.adapter = adapter

        fetchUserProfile { userId ->
            fetchPlaylists(userId) { playlists ->
                adapter.updateTracks(playlists)
            }
        }
    }

    private fun playPlaylist(playlistUri: String) {
        // Implement play playlist functionality here
        Log.d("MainActivity", "Playing playlist: $playlistUri")
    }

    private fun fetchUserProfile(onResult: (String) -> Unit) {
        val accessToken = getAccessToken(this)
        Log.d("MainActivity", "Using Access Token: $accessToken")
        if (accessToken.isNotEmpty()) {
            SpotifyApi.service.getCurrentUser("Bearer $accessToken").enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        val userId = response.body()?.id ?: return
                        onResult(userId)
                    } else {
                        Log.e("MainActivity", "Error fetching user ID: ${response.message()} Code: ${response.code()}")
                        Log.e("MainActivity", "Response body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    Log.e("MainActivity", "API call failed: ${t.message}")
                }
            })
        } else {
            Log.e("MainActivity", "Access token is missing")
        }
    }

    private fun fetchPlaylists(userId: String, onResult: (List<Playlist>) -> Unit) {
        val accessToken = getAccessToken(this)
        Log.d("MainActivity", "Using Access Token: $accessToken")
        if (accessToken.isNotEmpty()) {
            SpotifyApi.service.getUserPlaylists("Bearer $accessToken", userId).enqueue(object : Callback<PlaylistsResponse> {
                override fun onResponse(call: Call<PlaylistsResponse>, response: Response<PlaylistsResponse>) {
                    if (response.isSuccessful) {
                        val playlists = response.body()?.playlists ?: emptyList()
                        onResult(playlists)
                    } else {
                        Log.e("MainActivity", "Error fetching playlists: ${response.message()} Code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PlaylistsResponse>, t: Throwable) {
                    Log.e("MainActivity", "API call failed: ${t.message}")
                }
            })
        } else {
            Log.e("MainActivity", "Access token is missing")
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
}
