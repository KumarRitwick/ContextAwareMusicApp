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
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.RecommendationResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var popRecyclerView: RecyclerView
    private lateinit var rockRecyclerView: RecyclerView
    private lateinit var hipHopRecyclerView: RecyclerView
    private lateinit var audiobooksRecyclerView: RecyclerView

    private lateinit var popTrackAdapter: TrackAdapter
    private lateinit var rockTrackAdapter: TrackAdapter
    private lateinit var hipHopTrackAdapter: TrackAdapter
    private lateinit var audiobookAdapter: AudiobookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerViews and their adapters
        popRecyclerView = view.findViewById(R.id.pop_recycler_view)
        rockRecyclerView = view.findViewById(R.id.rock_recycler_view)
        hipHopRecyclerView = view.findViewById(R.id.hiphop_recycler_view)
        audiobooksRecyclerView = view.findViewById(R.id.audiobooks_recycler_view)

        popRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rockRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        hipHopRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        audiobooksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        popTrackAdapter = TrackAdapter(emptyList()) { track ->
            (activity as MainActivity).playPlaylist(track.uri)
        }
        rockTrackAdapter = TrackAdapter(emptyList()) { track ->
            (activity as MainActivity).playPlaylist(track.uri)
        }
        hipHopTrackAdapter = TrackAdapter(emptyList()) { track ->
            (activity as MainActivity).playPlaylist(track.uri)
        }
        audiobookAdapter = AudiobookAdapter(emptyList()) { audiobook ->
            // Handle audiobook click
        }

        popRecyclerView.adapter = popTrackAdapter
        rockRecyclerView.adapter = rockTrackAdapter
        hipHopRecyclerView.adapter = hipHopTrackAdapter
        audiobooksRecyclerView.adapter = audiobookAdapter

        // Fetch data for genres and audiobooks
        fetchRecommendations("pop", popTrackAdapter)
        fetchRecommendations("rock", rockTrackAdapter)
        fetchRecommendations("hip-hop", hipHopTrackAdapter)
        fetchAudiobooks()

        return view
    }

    private fun fetchRecommendations(genre: String, adapter: TrackAdapter) {
        val accessToken = getAccessToken(requireContext())
        val limit = 10

        SpotifyApi.service.getRecommendations("Bearer $accessToken", limit, genre).enqueue(object : Callback<RecommendationResponse> {
            override fun onResponse(call: Call<RecommendationResponse>, response: Response<RecommendationResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks ?: emptyList()
                    adapter.updateTracks(tracks)
                } else {
                    Log.e("HomeFragment", "Error fetching recommendations for $genre: ${response.message()}")
                    Toast.makeText(context, "Error fetching recommendations for $genre", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendationResponse>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAudiobooks() {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getAudiobooks("Bearer $accessToken").enqueue(object : Callback<AudiobooksResponse> {
            override fun onResponse(call: Call<AudiobooksResponse>, response: Response<AudiobooksResponse>) {
                if (response.isSuccessful) {
                    val audiobooks = response.body()?.audiobooks ?: emptyList()
                    Log.d("HomeFragment", "Audiobooks retrieved: ${audiobooks.size}")
                    audiobookAdapter.updateAudiobooks(audiobooks)
                } else {
                    // Log the full response to help diagnose the issue
                    Log.e("HomeFragment", "Error fetching audiobooks: ${response.message()} - ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Error fetching audiobooks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AudiobooksResponse>, t: Throwable) {
                Log.e("HomeFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun getAccessToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SpotifyCredential", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }
}
