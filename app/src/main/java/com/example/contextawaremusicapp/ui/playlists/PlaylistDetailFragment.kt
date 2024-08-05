package com.example.contextawaremusicapp.ui.playlistdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.MainActivity
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.model.TracksResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistDetailFragment : Fragment() {

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view)
        tracksRecyclerView.layoutManager = LinearLayoutManager(context)

        trackAdapter = TrackAdapter(listOf()) { track ->
            (activity as? MainActivity)?.playPlaylist(track.uri)
        }
        tracksRecyclerView.adapter = trackAdapter

        // Assuming playlistUri is passed through navigation arguments
        val playlistUri = arguments?.getString("playlistUri") ?: return view
        fetchPlaylistDetails(playlistUri)

        return view
    }

    private fun fetchPlaylistDetails(playlistUri: String) {
        val accessToken = (activity as? MainActivity)?.getAccessToken(requireContext())
        val playlistId = playlistUri.split(":").lastOrNull() ?: return

        // Fetch playlist details
        SpotifyApi.service.getPlaylistDetails("Bearer $accessToken", playlistId).enqueue(object : Callback<Playlist> {
            override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
                if (response.isSuccessful) {
                    val playlist = response.body()
                    playlist?.let {
                        loadPlaylistDetails(it)
                    }
                } else {
                    Log.e("PlaylistDetailFragment", "Error fetching playlist details: ${response.message()}")
                    Toast.makeText(context, "Error fetching playlist details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Playlist>, t: Throwable) {
                Log.e("PlaylistDetailFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch playlist tracks
        SpotifyApi.service.getPlaylistTracks("Bearer $accessToken", playlistId).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.items?.map { it.track } ?: emptyList()
                    trackAdapter.updateTracks(tracks)
                } else {
                    Log.e("PlaylistDetailFragment", "Error fetching playlist tracks: ${response.message()}")
                    Toast.makeText(context, "Error fetching playlist tracks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                Log.e("PlaylistDetailFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPlaylistDetails(playlist: Playlist) {
        // Update the UI with playlist details (name, image, etc.)
        val playlistImageView = view?.findViewById<ImageView>(R.id.playlist_image)
        val playlistNameTextView = view?.findViewById<TextView>(R.id.playlist_name)

        playlistNameTextView?.text = playlist.name
        if (playlist.images.isNotEmpty()) {
            if (playlistImageView != null) {
                Glide.with(this)
                    .load(playlist.images[0].url)
                    .into(playlistImageView)
            }
        } else {
            playlistImageView?.setImageResource(R.drawable.placeholder_image)
        }
    }
}
