package com.example.contextawaremusicapp.ui.home

import AudiobooksResponse
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import RecommendedPlaylistsResponse
import androidx.navigation.fragment.findNavController
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.WeatherResponse
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var workoutRecyclerView: RecyclerView
    private lateinit var jazzRecyclerView: RecyclerView
    private lateinit var audiobooksRecyclerView: RecyclerView

    private lateinit var moodAdapter: PlaylistAdapter
    private lateinit var workoutAdapter: PlaylistAdapter
    private lateinit var jazzAdapter: PlaylistAdapter
    private lateinit var audiobookAdapter: AudiobookAdapter

    private lateinit var recommendedPlaylistTitle: TextView
    private lateinit var recommendedPlaylistCover: ImageView
    private lateinit var recommendedPlaylistReason: TextView
    private var recommendedPlaylistUri: String? = null

    private val locationPermissionCode = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        recommendedPlaylistTitle = view.findViewById(R.id.recommended_playlist_title)
        recommendedPlaylistCover = view.findViewById(R.id.recommended_playlist_cover)
        recommendedPlaylistReason = view.findViewById(R.id.recommended_playlist_reason)

        view.findViewById<View>(R.id.recommended_playlist_container).setOnClickListener {
            recommendedPlaylistUri?.let { uri ->
                navigateToPlaylist(uri)
            }
        }

        moodRecyclerView = view.findViewById(R.id.mood_recycler_view)
        workoutRecyclerView = view.findViewById(R.id.workout_recycler_view)
        jazzRecyclerView = view.findViewById(R.id.jazz_recycler_view)
        audiobooksRecyclerView = view.findViewById(R.id.audiobooks_recycler_view)

        moodRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        workoutRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        jazzRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        audiobooksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        moodAdapter = PlaylistAdapter(emptyList()) { playlist ->
            navigateToPlaylist(playlist.id)
        }
        workoutAdapter = PlaylistAdapter(emptyList()) { playlist ->
            navigateToPlaylist(playlist.id)
        }
        jazzAdapter = PlaylistAdapter(emptyList()) { playlist ->
            navigateToPlaylist(playlist.id)
        }
        audiobookAdapter = AudiobookAdapter(emptyList()) { audiobook ->
            navigateToPlaylist(audiobook.uri)
        }

        moodRecyclerView.adapter = moodAdapter
        workoutRecyclerView.adapter = workoutAdapter
        jazzRecyclerView.adapter = jazzAdapter
        audiobooksRecyclerView.adapter = audiobookAdapter

        // Fetch data for categories and audiobooks
        fetchCategoryPlaylists("mood", moodAdapter)
        fetchCategoryPlaylists("workout", workoutAdapter)
        fetchCategoryPlaylists("jazz", jazzAdapter)
        fetchAudiobooksByIds()

        // Fetch recommended playlist based on time
        updateRecommendedPlaylist()

        // Check location permissions and fetch weather data
        checkLocationPermissionAndFetchWeather(view)

        return view
    }

    private fun fetchCategoryPlaylists(category: String, adapter: PlaylistAdapter?) {
        val accessToken = getAccessToken(requireContext())
        val limit = 10

        SpotifyApi.spotifyService.getCategoryPlaylists("Bearer $accessToken", category, limit, 0).enqueue(object : Callback<RecommendedPlaylistsResponse> {
            override fun onResponse(call: Call<RecommendedPlaylistsResponse>, response: Response<RecommendedPlaylistsResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.playlists?.items ?: emptyList()
                    adapter?.updatePlaylists(playlists)

                    // If it's the recommended category, display it on top
                    if (adapter == null && playlists.isNotEmpty()) {
                        val recommendedPlaylist = playlists[0]
                        recommendedPlaylistTitle.text = recommendedPlaylist.name
                        recommendedPlaylistUri = recommendedPlaylist.uri
                        Glide.with(this@HomeFragment)
                            .load(recommendedPlaylist.images.firstOrNull()?.url)
                            .into(recommendedPlaylistCover)
                    }
                } else {
                    Log.e("HomeFragment", "Error fetching playlists for $category: ${response.message()}")
                    Toast.makeText(context, "Error fetching playlists for $category", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendedPlaylistsResponse>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateRecommendedPlaylist() {
        val currentTimeCategory = getCurrentTimeCategory()

        val (category, title, reason) = when (currentTimeCategory) {
            TimeCategory.MORNING -> Triple("toplists", "Rise & Shine", "Start your day with energetic tunes")
            TimeCategory.AFTERNOON -> Triple("focus", "Work Mode On", "Stay focused with these tracks")
            TimeCategory.EVENING -> Triple("chill", "Unwind", "Relax after a long day")
            TimeCategory.NIGHT -> Triple("party", "Night Vibes", "Get the party started")
            else -> Triple("chill", "Unwind", "Enjoy a calming playlist")
        }

        recommendedPlaylistTitle.text = title
        recommendedPlaylistReason.text = reason
        fetchCategoryPlaylists(category, null)
    }

    private fun getCurrentTimeCategory(): TimeCategory {
        val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hourOfDay) {
            in 6..10 -> TimeCategory.MORNING
            in 11..15 -> TimeCategory.AFTERNOON
            in 16..19 -> TimeCategory.EVENING
            in 20..23 -> TimeCategory.NIGHT
            else -> TimeCategory.DEFAULT
        }
    }

    private fun fetchAudiobooksByIds() {
        val accessToken = getAccessToken(requireContext())
        val audiobookIds = "1QE2T94jOEXHUzw9t1bcOi,6dQDjeIzHGxg1Fy2Esr1Hb,5pveT2lEIPURW8nIzJrHvz,2kjaFU9MKm5WSJzjp1zYq8,0XJcPs6GB3FhRRStoUbCuL"

        SpotifyApi.spotifyService.getAudiobooksByIds("Bearer $accessToken", audiobookIds).enqueue(object : Callback<AudiobooksResponse> {
            override fun onResponse(call: Call<AudiobooksResponse>, response: Response<AudiobooksResponse>) {
                if (response.isSuccessful) {
                    val audiobooks = response.body()?.audiobooks
                        ?.filterNotNull()
                        ?.filter { it.images.isNotEmpty() } ?: emptyList()

                    Log.d("HomeFragment", "Audiobooks retrieved with album art: ${audiobooks.size}")

                    if (audiobooks.isNotEmpty()) {
                        audiobookAdapter.updateAudiobooks(audiobooks)
                    } else {
                        Log.e("HomeFragment", "No valid audiobooks with album art found")
                    }
                } else {
                    Log.e("HomeFragment", "Error fetching audiobooks: ${response.message()} - ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error fetching audiobooks", Toast.LENGTH_SHORT).show()
                    audiobookAdapter.updateAudiobooks(emptyList())
                }
            }

            override fun onFailure(call: Call<AudiobooksResponse>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
                audiobookAdapter.updateAudiobooks(emptyList())
            }
        })
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
                        val weatherCode = if (weatherResponse.current?.weather_code != 0) {
                            weatherResponse.current?.weather_code
                        } else {
                            weatherResponse.daily?.weather_code?.firstOrNull()
                        }

                        if (weatherCode != null) {
                            updateBackgroundImage(view, weatherCode)
                        } else {
                            Log.e("API_ERROR", "No valid weather code found")
                            updateBackgroundImage(view, -1)
                        }
                    } else {
                        Log.e("API_ERROR", "Error fetching weather data: Response is null")
                        updateBackgroundImage(view, -1)
                    }
                } else {
                    Log.e("API_ERROR", "Error fetching weather data: ${response.errorBody()?.string()}")
                    updateBackgroundImage(view, -1)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Weather API call failed: ${t.message}")
                updateBackgroundImage(view, -1)
            }
        })
    }

    private fun updateBackgroundImage(view: View, weatherCode: Int) {
        val backgroundImageResId = when (weatherCode) {
            1 -> R.drawable.clear_weather
            2 -> R.drawable.cloudy_weather
            3 -> R.drawable.rainy_weather
            4 -> R.drawable.snowy_weather
            5 -> R.drawable.stormy_weather
            else -> R.drawable.default_weather
        }

        val backgroundImageView: ImageView? = view.findViewById(R.id.background_image)
        if (backgroundImageView != null) {
            backgroundImageView.setImageResource(backgroundImageResId)
        } else {
            Log.e("HomeFragment", "ImageView with ID background_image not found")
        }
    }


    private fun getAccessToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SpotifyCredential", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }

    enum class TimeCategory {
        MORNING, AFTERNOON, EVENING, NIGHT, DEFAULT
    }

    private fun navigateToPlaylist(playlistId: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToPlaylistDetailFragment(playlistId)
        findNavController().navigate(action)
    }
}
