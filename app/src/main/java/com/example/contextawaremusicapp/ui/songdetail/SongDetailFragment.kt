package com.example.contextawaremusicapp.ui.songdetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.TracksResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongDetailFragment : Fragment() {

    private val args: SongDetailFragmentArgs by navArgs()

    private lateinit var albumArt: ImageView
    private lateinit var trackName: TextView
    private lateinit var trackArtist: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_detail, container, false)

        albumArt = view.findViewById(R.id.album_art)
        trackName = view.findViewById(R.id.track_name)
        trackArtist = view.findViewById(R.id.track_artist)

        // Fetch and display song details using the playlist URI
        val playlistUri = args.playlistUri
        fetchSongDetails(playlistUri)

        return view
    }

    private fun fetchSongDetails(trackUri: String) {
        val trackId = trackUri.split(":").last() // Extract track ID from URI
        val accessToken = getAccessToken(requireContext())

        SpotifyApi.service.getTracks("Bearer $accessToken", trackId).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val track = response.body()?.tracks?.firstOrNull()
                    track?.let { displayTrackDetails(it) }
                } else {
                    // Handle error
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun displayTrackDetails(track: Track) {
        trackName.text = track.name
        trackArtist.text = track.artists.joinToString(", ") { it.name }
        if (track.album.images.isNotEmpty()) {
            Glide.with(this)
                .load(track.album.images[0].url)
                .into(albumArt)
        }
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
}
