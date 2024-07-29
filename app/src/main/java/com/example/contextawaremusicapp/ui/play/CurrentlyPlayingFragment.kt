package com.example.contextawaremusicapp.ui.currentlyplaying

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager

class CurrentlyPlayingFragment : Fragment() {

    private val args: CurrentlyPlayingFragmentArgs by navArgs()

    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var albumImage: ImageView
    private lateinit var playPauseButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_currently_playing, container, false)

        trackName = view.findViewById(R.id.track_name)
        artistName = view.findViewById(R.id.artist_name)
        albumImage = view.findViewById(R.id.album_image)
        playPauseButton = view.findViewById(R.id.play_pause_button)

        val track = args.track

        displayTrackDetails(track)
        setupPlayPauseButton()

        return view
    }

    private fun displayTrackDetails(track: Track) {
        trackName.text = track.name
        artistName.text = track.artists.joinToString(", ") { it.name }
        if (track.album.images.isNotEmpty()) {
            Glide.with(this)
                .load(track.album.images[0].url)
                .into(albumImage)
        }
    }

    private fun setupPlayPauseButton() {
        playPauseButton.setOnClickListener {
            SpotifyRemoteManager.togglePlayPause({
                updatePlayPauseButton()
            }, { error ->
                // Handle error
            })
        }
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        SpotifyRemoteManager.getPlayerState { playerState ->
            playPauseButton.text = if (playerState.isPaused) "Play" else "Pause"
        }
    }
}
