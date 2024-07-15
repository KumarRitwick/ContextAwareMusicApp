package com.example.contextawaremusicapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.PlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.ui.theme.ContextAwareMusicAppTheme
import androidx.compose.foundation.layout.fillMaxSize
import com.example.contextawaremusicapp.controller.AuthActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    var flag : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!flag) {
            flag = true
            var intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
        setContent {
            ContextAwareMusicAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaylistScreen(modifier = Modifier.padding(innerPadding), context = this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun PlaylistScreen(modifier: Modifier = Modifier, context: Context) {
    var playlists by remember { mutableStateOf(listOf<Playlist>()) }

    LaunchedEffect(Unit) {
        val accessToken = getAccessToken(context)
        Log.d("PlaylistScreen", "Using Access Token: $accessToken")
        if (accessToken.isNotEmpty()) {
            SpotifyApi.service.getUserPlaylists("Bearer $accessToken").enqueue(object : Callback<PlaylistsResponse> {
                override fun onResponse(call: Call<PlaylistsResponse>, response: Response<PlaylistsResponse>) {
                    if (response.isSuccessful) {
                        playlists = response.body()?.playlists ?: emptyList()
                    } else {
                        Log.e("PlaylistScreen", "Error fetching playlists: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<PlaylistsResponse>, t: Throwable) {
                    Log.e("PlaylistScreen", "API call failed: ${t.message}")
                }
            })
        } else {
            Log.e("PlaylistScreen", "Access token is missing")
        }
    }

    LazyColumn(modifier = modifier) {
        items(playlists) { playlist ->
            Text(text = "Playlist: ${playlist.name}")
        }
    }
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

@Preview(showBackground = true)
@Composable
fun PreviewPlaylistScreen() {
    ContextAwareMusicAppTheme {
        PlaylistScreen(context = LocalContext.current)
    }
}
