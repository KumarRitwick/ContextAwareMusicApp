package com.example.contextawaremusicapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

object SpotifyRemoteManager {

    private const val CLIENT_ID = "cffc2e76239543e18c44e888d655a3a3"
    private const val REDIRECT_URI = "contextawaremusicapp://callback"
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

    fun play(uri: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        Log.d("SpotifyRemoteManager", "Attempting to play URI: $uri")
        spotifyAppRemote?.playerApi?.play(uri)?.setResultCallback {
            Log.d("SpotifyRemoteManager", "Successfully started playing URI: $uri")
            onSuccess()
        }?.setErrorCallback {
            Log.e("SpotifyRemoteManager", "Error playing URI: $uri", it)
            onError(it)
        } ?: run {
            Log.e("SpotifyRemoteManager", "Spotify App Remote is not connected")
            onError(Throwable("Spotify App Remote is not connected"))
        }
    }

    private fun promptInstallSpotify(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
