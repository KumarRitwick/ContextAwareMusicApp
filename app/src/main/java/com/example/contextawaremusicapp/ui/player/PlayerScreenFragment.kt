package com.example.contextawaremusicapp.ui.player

import SpotifyQueueResponse
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.MainActivity
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.Track
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayerScreenFragment : Fragment() {

    private lateinit var albumArt: ImageView
    private lateinit var trackTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var trackProgress: SeekBar
    private lateinit var previousButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var queueRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

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
        queueRecyclerView = view.findViewById(R.id.queue_recycler_view)

        queueRecyclerView.layoutManager = LinearLayoutManager(context)
        trackAdapter = TrackAdapter(emptyList()) { track ->
            playSelectedTrack(track.uri)
        }
        queueRecyclerView.adapter = trackAdapter

        setupPlayerControls()
        fetchUserQueue()

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

        val imageUri: ImageUri = playerState.track.imageUri

        if (imageUri.raw?.isNotEmpty() == true) {
            SpotifyRemoteManager.spotifyAppRemote?.imagesApi?.getImage(imageUri)?.setResultCallback { bitmap ->
                albumArt.setImageBitmap(bitmap)
            }?.setErrorCallback {
                albumArt.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            albumArt.setImageResource(R.drawable.placeholder_image)
        }

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
            playPauseButton.setImageResource(R.drawable.ic_play_black)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_pause_black)
        }
    }

    private fun fetchUserQueue() {
        val accessToken = (activity as? MainActivity)?.getAccessToken(requireContext()) ?: return

        SpotifyApi.service.getUserQueue("Bearer $accessToken").enqueue(object : Callback<SpotifyQueueResponse> {
            override fun onResponse(call: Call<SpotifyQueueResponse>, response: Response<SpotifyQueueResponse>) {
                if (response.isSuccessful) {
                    val queueResponse = response.body()

                    // Log the entire response
                    Log.d("API_RESPONSE", "Queue Response: $queueResponse")

                    queueResponse?.currentlyPlaying?.let { currentlyPlayingTrack ->
                        trackTitle.text = currentlyPlayingTrack.name
                        artistName.text = currentlyPlayingTrack.artists.joinToString(separator = ", ") { it.name }
                        if (currentlyPlayingTrack.album.images.isNotEmpty()) {
                            Glide.with(albumArt.context)
                                .load(currentlyPlayingTrack.album.images[0].url)
                                .into(albumArt)
                        } else {
                            albumArt.setImageResource(R.drawable.placeholder_image)
                        }
                    }

                    // Extract and log the queue to see if it contains duplicates
                    val queue = queueResponse?.queue ?: emptyList()
                    Log.d("QUEUE_DEBUG", "Queue size: ${queue.size}, Queue items: $queue")

                    trackAdapter.updateTracks(queue)
                } else {
                    Log.e("API_ERROR", "Error in response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SpotifyQueueResponse>, t: Throwable) {
                Log.e("API_FAILURE", "API call failed: ${t.message}")
            }
        })
    }



    private fun playSelectedTrack(trackUri: String) {
        lifecycleScope.launch {
            SpotifyRemoteManager.playTrack(trackUri)
            fetchUserQueue()
        }
    }
}
