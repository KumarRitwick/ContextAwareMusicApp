package com.example.contextawaremusicapp.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerScreenFragment : Fragment() {

    private lateinit var albumArt: ImageView
    private lateinit var trackTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var trackProgress: SeekBar
    private lateinit var previousButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton

    private var playerStateSubscription: Subscription<PlayerState>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_screen, container, false)

        albumArt = view.findViewById(R.id.album_art)
        trackTitle = view.findViewById(R.id.track_title)
        artistName = view.findViewById(R.id.artist_name)
        trackProgress = view.findViewById(R.id.track_progress)
        previousButton = view.findViewById(R.id.previous_button)
        playPauseButton = view.findViewById(R.id.play_pause_button)
        nextButton = view.findViewById(R.id.next_button)

        setupPlayerControls()

        return view
    }

    override fun onStart() {
        super.onStart()
        observePlayerState()
    }

    override fun onStop() {
        super.onStop()
        playerStateSubscription?.cancel()
    }

    private fun setupPlayerControls() {
        previousButton.setOnClickListener {
            SpotifyRemoteManager.skipToPrevious()
        }

        playPauseButton.setOnClickListener {
            SpotifyRemoteManager.togglePlayPause()
        }

        nextButton.setOnClickListener {
            SpotifyRemoteManager.skipToNext()
        }

        trackProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SpotifyRemoteManager.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun observePlayerState() {
        playerStateSubscription = SpotifyRemoteManager.subscribeToPlayerState { playerState ->
            updatePlayerUI(playerState)
        }
    }

    private fun updatePlayerUI(playerState: PlayerState) {
        trackTitle.text = playerState.track.name
        artistName.text = playerState.track.artist.name

        playerState.track.imageUri.raw?.let { imageUri ->
            if (imageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUri)
                    .into(albumArt)
            } else {
                albumArt.setImageResource(R.drawable.placeholder_image)
            }
        } ?: albumArt.setImageResource(R.drawable.placeholder_image)


        trackProgress.max = playerState.track.duration.toInt()
        trackProgress.progress = playerState.playbackPosition.toInt()

        lifecycleScope.launch {
            while (true) {
                val playerState = SpotifyRemoteManager.getPlayerState()
                if (playerState?.isPaused == false) {
                    val progress = playerState.playbackPosition.toInt()
                    trackProgress.progress = progress
                }
                delay(1000)
            }
        }

        if (playerState.isPaused) {
            playPauseButton.setImageResource(R.drawable.ic_play)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause)
        }
    }
}
