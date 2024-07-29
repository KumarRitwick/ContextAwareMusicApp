package com.example.contextawaremusicapp.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.GenreSection
import com.example.contextawaremusicapp.controller.GenreSectionAdapter
import com.example.contextawaremusicapp.model.RecommendationResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var genreSectionAdapter: GenreSectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.home_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchRecommendations()

        return view
    }

    private fun fetchRecommendations() {
        val accessToken = getAccessToken(requireContext())
        val genres = listOf("pop", "rock", "hip-hop")
        val limit = 10
        val genreSections = mutableListOf<GenreSection>()

        genres.forEach { genre ->
            SpotifyApi.service.getRecommendations("Bearer $accessToken", limit, genre).enqueue(object : Callback<RecommendationResponse> {
                override fun onResponse(call: Call<RecommendationResponse>, response: Response<RecommendationResponse>) {
                    if (response.isSuccessful) {
                        val tracks = response.body()?.tracks ?: emptyList()
                        genreSections.add(GenreSection(genre, tracks))
                        if (genreSections.size == genres.size) {
                            genreSectionAdapter = GenreSectionAdapter(genreSections) { track ->
                                navigateToCurrentlyPlaying(track)
                            }
                            recyclerView.adapter = genreSectionAdapter
                        }
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
    }

    private fun navigateToCurrentlyPlaying(track: Track) {
        val action = HomeFragmentDirections.actionNavigationHomeToCurrentlyPlaying(track)
        findNavController().navigate(action)
    }

    private fun getAccessToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SpotifyCredential", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }
}
