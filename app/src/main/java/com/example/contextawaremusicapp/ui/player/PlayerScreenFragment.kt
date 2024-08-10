package com.example.contextawaremusicapp.ui.player

import SpotifyQueueResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.MainActivity
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.WeatherResponse
import com.example.contextawaremusicapp.utils.SpotifyRemoteManager
import com.google.android.gms.location.LocationServices
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class PlayerScreenFragment : Fragment() {

    private lateinit var albumArt: ImageView
    private lateinit var trackTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var trackProgress: SeekBar
    private lateinit var previousButton: ImageButton
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var queueRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var isShuffleEnabled = false
    private val locationPermissionCode = 101

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
        shuffleButton = view.findViewById(R.id.shuffle_button)
        queueRecyclerView = view.findViewById(R.id.queue_recycler_view)

        queueRecyclerView.layoutManager = LinearLayoutManager(context)
        trackAdapter = TrackAdapter(emptyList()) { track ->
            playSelectedTrack(track.uri)
        }
        queueRecyclerView.adapter = trackAdapter

        setupPlayerControls()
        fetchUserQueue()
        checkLocationPermissionAndFetchWeather(view)

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

        shuffleButton.setOnClickListener {
            toggleShuffle()
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

    private fun toggleShuffle() {
        val accessToken = (activity as? MainActivity)?.getAccessToken(requireContext()) ?: return

        isShuffleEnabled = !isShuffleEnabled

        SpotifyApi.spotifyService.setShuffleState("Bearer $accessToken", isShuffleEnabled).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    val message = if (isShuffleEnabled) "Shuffle Enabled" else "Shuffle Disabled"
                    Log.d("SHUFFLE_STATUS", message)
                    updateShuffleButtonUI()
                    fetchUserQueue()
                } else {
                    handleShuffleError(response)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API_FAILURE", "Shuffle API call failed: ${t.message}")
            }
        })
    }

    private fun handleShuffleError(response: Response<Void>) {
        if (response.code() == 403) {
            val errorBody = response.errorBody()?.string()
            Log.e("API_ERROR", "Error in shuffle response: $errorBody")
            if (errorBody?.contains("Restriction violated") == true) {
                showMessageToUser("Shuffle command not allowed. This could be due to your current Spotify plan or context.")
            } else {
                showMessageToUser("Shuffle command failed. Please try again later.")
            }
        } else {
            Log.e("API_ERROR", "Unexpected error in shuffle response: ${response.errorBody()?.string()}")
        }
    }

    private fun showMessageToUser(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateShuffleButtonUI() {
        if (isShuffleEnabled) {
            shuffleButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        } else {
            shuffleButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
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

        SpotifyApi.spotifyService.getUserQueue("Bearer $accessToken").enqueue(object : Callback<SpotifyQueueResponse> {
            override fun onResponse(call: Call<SpotifyQueueResponse>, response: Response<SpotifyQueueResponse>) {
                if (response.isSuccessful) {
                    val queueResponse = response.body()

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

    private fun checkLocationPermissionAndFetchWeather(view: View) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            fetchLastKnownLocation(view)
        }
    }

    private fun fetchLastKnownLocation(view: View) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    fetchWeatherDataAndUpdateBackground(view, it.latitude, it.longitude)
                } ?: Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherDataAndUpdateBackground(view: View, latitude: Double, longitude: Double) {
        SpotifyApi.openMeteoService.getWeather(latitude, longitude).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        val weatherCode = weatherResponse.current?.weather_code
                            ?: weatherResponse.hourly?.weather_code?.firstOrNull()

                        if (weatherCode != null) {
                            updateBackgroundColor(view, weatherCode)
                        } else {
                            Log.e("API_ERROR", "Weather data is null")
                        }
                    } else {
                        Log.e("API_ERROR", "Error fetching weather data: ${response.errorBody()?.string()}")
                    }
                } else {
                    Log.e("API_ERROR", "Error fetching weather data: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Weather API call failed: ${t.message}")
            }
        })
    }

    private fun updateBackgroundColor(view: View, weatherCode: Int) {
        val backgroundColorResId = when (weatherCode) {
            1 -> R.color.clearWeather
            2 -> R.color.cloudyWeather
            3 -> R.color.rainyWeather
            4 -> R.color.snowyWeather
            5 -> R.color.stormyWeather
            else -> R.color.defaultWeather
        }

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColorResId))
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLastKnownLocation(requireView())
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
