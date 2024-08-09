package com.example.contextawaremusicapp.ui.home

import AudiobooksResponse
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import RecommendedPlaylistsResponse
import androidx.navigation.fragment.findNavController
import com.example.contextawaremusicapp.model.SpotifyApi
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
    private var recommendedPlaylistUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize recommended playlist views
        recommendedPlaylistTitle = view.findViewById(R.id.recommended_playlist_title)
        recommendedPlaylistCover = view.findViewById(R.id.recommended_playlist_cover)

        // Set up click listener for recommended playlist
        view.findViewById<View>(R.id.recommended_playlist_container).setOnClickListener {
            recommendedPlaylistUri?.let { uri ->
                navigateToPlaylist(uri)
            }
        }

        // Initialize RecyclerViews and their adapters
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

        return view
    }

    private fun fetchCategoryPlaylists(category: String, adapter: PlaylistAdapter?) {
        val accessToken = getAccessToken(requireContext())
        val limit = 10

        SpotifyApi.service.getCategoryPlaylists("Bearer $accessToken", category, limit, 0).enqueue(object : Callback<RecommendedPlaylistsResponse> {
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

        val category = when (currentTimeCategory) {
            TimeCategory.MORNING -> "toplists"
            TimeCategory.AFTERNOON -> "focus"
            TimeCategory.EVENING -> "chill"
            TimeCategory.NIGHT -> "party"
            else -> "chill"
        }

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

        SpotifyApi.service.getAudiobooksByIds("Bearer $accessToken", audiobookIds).enqueue(object : Callback<AudiobooksResponse> {
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
