package com.example.contextawaremusicapp.ui.playlists

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.PlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.model.TracksResponse
import com.example.contextawaremusicapp.model.UserResponse
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)

        recyclerView = view.findViewById(R.id.playlists_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PlaylistAdapter(listOf()) { playlistUri ->
            fetchPlaylistTracks(playlistUri)
        }
        recyclerView.adapter = adapter

        fetchUserPlaylists()

        return view
    }

    private fun fetchUserPlaylists() {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getCurrentUser("Bearer $accessToken").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.id ?: return
                    fetchPlaylists(userId)
                } else {
                    if (response.code() == 401) {
                        refreshToken {
                            fetchUserPlaylists()
                        }
                    } else {
                        Log.e("PlaylistFragment", "Error fetching user ID: ${response.message()}")
                        Toast.makeText(context, "Error fetching user ID", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPlaylists(userId: String) {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getUserPlaylists("Bearer $accessToken", userId).enqueue(object : Callback<PlaylistsResponse> {
            override fun onResponse(call: Call<PlaylistsResponse>, response: Response<PlaylistsResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.playlists ?: emptyList()
                    adapter.updateTracks(playlists)
                } else {
                    if (response.code() == 401) {
                        refreshToken {
                            fetchPlaylists(userId)
                        }
                    } else {
                        Log.e("PlaylistFragment", "Error fetching playlists: ${response.message()} Code: ${response.code()}")
                        Toast.makeText(context, "Error fetching playlists", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<PlaylistsResponse>, t: Throwable) {
                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPlaylistTracks(playlistUri: String) {
        val playlistId = playlistUri.split(":").lastOrNull() ?: return
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getPlaylistTracks("Bearer $accessToken", playlistId).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val trackItems = response.body()?.items ?: emptyList()
                    if (trackItems.isNotEmpty()) {
                        val track = trackItems[0].track
                        navigateToCurrentlyPlaying(track)
                    } else {
                        Log.e("PlaylistFragment", "No tracks found in playlist")
                        Toast.makeText(context, "No tracks found in playlist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (response.code() == 401) {
                        refreshToken {
                            fetchPlaylistTracks(playlistUri)
                        }
                    } else {
                        Log.e("PlaylistFragment", "Error fetching playlist tracks: ${response.message()}")
                        Toast.makeText(context, "Error fetching playlist tracks", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToCurrentlyPlaying(track: Track) {
        val action = PlaylistFragmentDirections.actionPlaylistsToCurrentlyPlaying(track)
        findNavController().navigate(action)
    }

    private fun getAccessToken(context: Context): String {
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

        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }

    private fun refreshToken(onTokenRefreshed: () -> Unit) {
        SpotifyRemoteManager.refreshToken(requireContext(), { newToken ->
            onTokenRefreshed()
        }, { error ->
            Log.e("PlaylistFragment", "Error refreshing token", error)
            Toast.makeText(context, "Error refreshing token", Toast.LENGTH_SHORT).show()
        })
    }
}
