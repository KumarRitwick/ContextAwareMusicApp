package com.example.contextawaremusicapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object SpotifyRemoteManager {

    private const val CLIENT_ID = "cffc2e76239543e18c44e888d655a3a3"
    private const val CLIENT_SECRET = "ad760e57f4de4a619fbaadadb9685e55"
    private const val REDIRECT_URI = "contextawaremusicapp://callback"
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    private var spotifyAppRemote: SpotifyAppRemote? = null

    fun connect(context: Context, onConnected: () -> Unit, onFailure: (Throwable) -> Unit) {
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                Log.d("SpotifyRemoteManager", "Connected to Spotify App Remote")
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                if (throwable is CouldNotFindSpotifyApp) {
                    Log.e("SpotifyRemoteManager", "Spotify app is not installed", throwable)
                    promptInstallSpotify(context)
                } else {
                    Log.e("SpotifyRemoteManager", "Failed to connect to Spotify App Remote", throwable)
                }
                onFailure(throwable)
            }
        })
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            Log.d("SpotifyRemoteManager", "Disconnecting from Spotify App Remote")
            SpotifyAppRemote.disconnect(it)
        }
    }

    suspend fun playTrack(trackUri: String) = withContext(Dispatchers.IO) {
        spotifyAppRemote?.playerApi?.play(trackUri)
    }

    suspend fun pausePlayback() = withContext(Dispatchers.IO) {
        spotifyAppRemote?.playerApi?.pause()
    }

    suspend fun resumePlayback() = withContext(Dispatchers.IO) {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun skipToNext() {
        spotifyAppRemote?.playerApi?.skipNext()
    }

    fun skipToPrevious() {
        spotifyAppRemote?.playerApi?.skipPrevious()
    }

    fun togglePlayPause() {
        spotifyAppRemote?.let {
            it.playerApi?.playerState?.setResultCallback { playerState ->
                if (playerState.isPaused) {
                    it.playerApi?.resume()
                } else {
                    it.playerApi?.pause()
                }
            }
        }
    }

    fun seekTo(position: Long) {
        spotifyAppRemote?.playerApi?.seekTo(position)
    }

    suspend fun getPlayerState(): PlayerState? = withContext(Dispatchers.IO) {
        spotifyAppRemote?.playerApi?.playerState?.awaitOrNull()
    }

    private suspend fun <T> CallResult<T>.awaitOrNull(): T? {
        return try {
            this.await().data
        } catch (e: Exception) {
            null
        }
    }

    fun refreshToken(context: Context, onTokenRefreshed: (String) -> Unit, onError: (Throwable) -> Unit) {
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

        val refreshToken = sharedPreferences.getString("REFRESH_TOKEN", "") ?: ""
        if (refreshToken.isEmpty()) {
            onError(Throwable("Refresh token is missing"))
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(TOKEN_URL)
            .addHeader("Authorization", "Basic " + android.util.Base64.encodeToString("$CLIENT_ID:$CLIENT_SECRET".toByteArray(), android.util.Base64.NO_WRAP))
            .post(
                okhttp3.FormBody.Builder()
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", refreshToken)
                    .build()
            )
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                onError(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    onError(Throwable("Failed to refresh token"))
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val newAccessToken = json.getString("access_token")
                    sharedPreferences.edit().putString("ACCESS_TOKEN", newAccessToken).apply()
                    onTokenRefreshed(newAccessToken)
                } else {
                    onError(Throwable("Failed to parse refresh token response"))
                }
            }
        })
    }

    private fun promptInstallSpotify(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun observeTrackChanges(onTrackChanged: (String) -> Unit) {
        spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { playerState ->
            val trackName = playerState.track?.name ?: "Unknown Track"
            onTrackChanged(trackName)
        }
    }

    fun subscribeToPlayerState(onPlayerStateChanged: (PlayerState) -> Unit): Subscription<PlayerState>? {
        return spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback(onPlayerStateChanged)
    }
}
