package com.example.contextawaremusicapp.ui.home

import AudiobooksResponse
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contextawaremusicapp.MainActivity
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import RecommendedPlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var topListsRecyclerView: RecyclerView
    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var workoutRecyclerView: RecyclerView
    private lateinit var chillRecyclerView: RecyclerView
    private lateinit var focusRecyclerView: RecyclerView
    private lateinit var partyRecyclerView: RecyclerView
    private lateinit var jazzRecyclerView: RecyclerView
    private lateinit var audiobooksRecyclerView: RecyclerView

    private lateinit var topListsAdapter: PlaylistAdapter
    private lateinit var moodAdapter: PlaylistAdapter
    private lateinit var workoutAdapter: PlaylistAdapter
    private lateinit var chillAdapter: PlaylistAdapter
    private lateinit var focusAdapter: PlaylistAdapter
    private lateinit var partyAdapter: PlaylistAdapter
    private lateinit var jazzAdapter: PlaylistAdapter
    private lateinit var audiobookAdapter: AudiobookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerViews and their adapters
        topListsRecyclerView = view.findViewById(R.id.toplists_recycler_view)
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view)
        workoutRecyclerView = view.findViewById(R.id.workout_recycler_view)
        chillRecyclerView = view.findViewById(R.id.chill_recycler_view)
        focusRecyclerView = view.findViewById(R.id.focus_recycler_view)
        partyRecyclerView = view.findViewById(R.id.party_recycler_view)
        jazzRecyclerView = view.findViewById(R.id.jazz_recycler_view)
        audiobooksRecyclerView = view.findViewById(R.id.audiobooks_recycler_view)

        // Set layout managers for each RecyclerView
        topListsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        moodRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        workoutRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        chillRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        focusRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        partyRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        jazzRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        audiobooksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Initialize Adapters
        topListsAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        moodAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        workoutAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        chillAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        focusAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        partyAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        jazzAdapter = PlaylistAdapter(emptyList()) { playlist ->
            (activity as MainActivity).playPlaylist(playlist.uri)
        }
        audiobookAdapter = AudiobookAdapter(emptyList()) { audiobook ->
            (activity as MainActivity).playAudiobook(audiobook.uri)
        }

        // Set Adapters to RecyclerViews
        topListsRecyclerView.adapter = topListsAdapter
        moodRecyclerView.adapter = moodAdapter
        workoutRecyclerView.adapter = workoutAdapter
        chillRecyclerView.adapter = chillAdapter
        focusRecyclerView.adapter = focusAdapter
        partyRecyclerView.adapter = partyAdapter
        jazzRecyclerView.adapter = jazzAdapter
        audiobooksRecyclerView.adapter = audiobookAdapter

        // Fetch data for categories and audiobooks
        fetchCategoryPlaylists("toplists", topListsAdapter)
        fetchCategoryPlaylists("mood", moodAdapter)
        fetchCategoryPlaylists("workout", workoutAdapter)
        fetchCategoryPlaylists("chill", chillAdapter)
        fetchCategoryPlaylists("focus", focusAdapter)
        fetchCategoryPlaylists("party", partyAdapter)
        fetchCategoryPlaylists("jazz", jazzAdapter)
        fetchAudiobooksByIds()

        return view
    }

    private fun fetchCategoryPlaylists(category: String, adapter: PlaylistAdapter) {
        val accessToken = getAccessToken(requireContext())

        SpotifyApi.service.getCategoryPlaylists("Bearer $accessToken", category).enqueue(object : Callback<RecommendedPlaylistsResponse> {
            override fun onResponse(call: Call<RecommendedPlaylistsResponse>, response: Response<RecommendedPlaylistsResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.playlists?.items ?: emptyList()
                    adapter.updatePlaylists(playlists)
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
}
