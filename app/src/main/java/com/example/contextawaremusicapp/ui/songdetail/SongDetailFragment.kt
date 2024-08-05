package com.example.contextawaremusicapp.ui.songdetail

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.TracksResponse
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongDetailFragment : Fragment() {

    private val args: SongDetailFragmentArgs by navArgs()

    private lateinit var albumArt: ImageView
    private lateinit var trackName: TextView
    private lateinit var trackArtist: TextView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_detail, container, false)

        albumArt = view.findViewById(R.id.album_art)
        trackName = view.findViewById(R.id.track_name)
        trackArtist = view.findViewById(R.id.track_artist)
        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view)

        tracksRecyclerView.layoutManager = LinearLayoutManager(context)
        trackAdapter = TrackAdapter(listOf()) { track ->
            viewLifecycleOwner.lifecycleScope.launch {
                SpotifyRemoteManager.playTrack(track.uri)
            }
        }
        tracksRecyclerView.adapter = trackAdapter

        val playlistId = args.playlistUri.split(":").lastOrNull()
        if (playlistId != null) {
            fetchPlaylistDetails(playlistId)
            fetchPlaylistTracks(playlistId)
        } else {
            Log.e("SongDetailFragment", "Invalid playlist URI: ${args.playlistUri}")
        }

        return view
    }

    private fun fetchPlaylistDetails(playlistId: String) {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getPlaylistDetails("Bearer $accessToken", playlistId).enqueue(object : Callback<Playlist> {
            override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
                if (response.isSuccessful) {
                    val playlist = response.body()
                    if (playlist != null) {
                        displayPlaylistDetails(playlist)
                    } else {
                        Log.e("SongDetailFragment", "No playlist details found")
                    }
                } else {
                    Log.e("SongDetailFragment", "Error fetching playlist details: ${response.message()}")
                    Toast.makeText(context, "Error fetching playlist details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Playlist>, t: Throwable) {
                Log.e("SongDetailFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPlaylistTracks(playlistId: String) {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getPlaylistTracks("Bearer $accessToken", playlistId).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val trackItems = response.body()?.items
                    if (trackItems != null && trackItems.isNotEmpty()) {
                        val tracks = trackItems.map { it.track }
                        Log.d("SongDetailFragment", "Tracks found: ${tracks.size}")
                        displayTrackDetails(tracks)
                    } else {
                        Log.e("SongDetailFragment", "No tracks found in playlist")
                        Toast.makeText(context, "No tracks found in playlist", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SongDetailFragment", "Error fetching playlist tracks: ${response.message()}")
                    if (response.code() == 401) {
                        refreshToken {
                            fetchPlaylistTracks(playlistId)
                        }
                    } else {
                        Toast.makeText(context, "Error fetching playlist tracks", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                Log.e("SongDetailFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayPlaylistDetails(playlist: Playlist) {
        trackName.text = playlist.name
        if (playlist.images.isNotEmpty()) {
            Glide.with(this)
                .load(playlist.images[0].url)
                .into(albumArt)
        } else {
            albumArt.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun displayTrackDetails(tracks: List<Track>) {
        trackAdapter.updateTracks(tracks)
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
            Log.d("SongDetailFragment", "Token refreshed successfully")
            onTokenRefreshed()
        }, { error ->
            Log.e("SongDetailFragment", "Error refreshing token", error)
            Toast.makeText(context, "Error refreshing token", Toast.LENGTH_SHORT).show()
        })
    }
}
