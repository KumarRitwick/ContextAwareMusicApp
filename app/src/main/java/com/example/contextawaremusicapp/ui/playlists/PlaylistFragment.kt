package com.example.contextawaremusicapp.ui.playlists

import RecommendedPlaylistsResponse
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
import com.example.contextawaremusicapp.controller.PlaylistAdapter
import com.example.contextawaremusicapp.model.Playlist
import com.example.contextawaremusicapp.model.PlaylistsResponse
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistFragment : Fragment() {

    private lateinit var userPlaylistsRecyclerView: RecyclerView
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var userPlaylistAdapter: PlaylistAdapter
    private lateinit var recommendationsAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)

        userPlaylistsRecyclerView = view.findViewById(R.id.user_playlists_recycler_view)
        recommendationsRecyclerView = view.findViewById(R.id.recommendations_recycler_view)

        userPlaylistsRecyclerView.layoutManager = LinearLayoutManager(context)
        recommendationsRecyclerView.layoutManager = LinearLayoutManager(context)

        userPlaylistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            // Navigate to PlaylistDetailFragment
            val action = PlaylistFragmentDirections.actionPlaylistFragmentToPlaylistDetailFragment(playlist.uri)
            findNavController().navigate(action)
        }

        // Initializing the adapter with empty list
        recommendationsAdapter = PlaylistAdapter(emptyList()) { playlist ->
            // Navigate to PlaylistDetailFragment
            val action = PlaylistFragmentDirections.actionPlaylistFragmentToPlaylistDetailFragment(playlist.uri)
            findNavController().navigate(action)
        }

        userPlaylistsRecyclerView.adapter = userPlaylistAdapter
        recommendationsRecyclerView.adapter = recommendationsAdapter

        fetchUserPlaylists()

        // Fetch different categories of playlists and add headers
        fetchCategoryPlaylists("toplists", "Top Lists")
        fetchCategoryPlaylists("mood", "Mood")
        fetchCategoryPlaylists("workout", "Workout")
        fetchCategoryPlaylists("chill", "Chill")
        fetchCategoryPlaylists("focus", "Focus")
        fetchCategoryPlaylists("party", "Party")
        fetchCategoryPlaylists("jazz", "Jazz")

        return view
    }

    private fun getAccessToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SpotifyCredential", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }

    private fun fetchUserPlaylists() {
        val accessToken = getAccessToken(requireContext())
        SpotifyApi.service.getCurrentUser("Bearer $accessToken").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.id
                    if (userId != null) {
                        // Fetch the user's playlists
                        SpotifyApi.service.getUserPlaylists("Bearer $accessToken", userId).enqueue(object : Callback<PlaylistsResponse> {
                            override fun onResponse(call: Call<PlaylistsResponse>, response: Response<PlaylistsResponse>) {
                                if (response.isSuccessful) {
                                    val playlists = response.body()?.playlists ?: emptyList()
                                    userPlaylistAdapter.updatePlaylists(playlists)
                                } else {
                                    Log.e("PlaylistFragment", "Error fetching playlists: ${response.message()}")
                                    Toast.makeText(context, "Error fetching playlists", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<PlaylistsResponse>, t: Throwable) {
                                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } else {
                    Log.e("PlaylistFragment", "Error fetching user ID: ${response.message()}")
                    Toast.makeText(context, "Error fetching user ID", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategoryPlaylists(categoryId: String, headerTitle: String) {
        val accessToken = getAccessToken(requireContext())
        val limit = 2
        val offset = 0

        SpotifyApi.service.getCategoryPlaylists("Bearer $accessToken", categoryId, limit, offset).enqueue(object : Callback<RecommendedPlaylistsResponse> {
            override fun onResponse(call: Call<RecommendedPlaylistsResponse>, response: Response<RecommendedPlaylistsResponse>) {
                if (response.isSuccessful) {
                    val playlists = response.body()?.playlists?.items?.map { playlist ->
                        playlist.copy(category = categoryId)
                    } ?: emptyList()

                    if (playlists.isNotEmpty()) {
                        recommendationsAdapter.addHeaderAndPlaylists(headerTitle, playlists)
                    } else {
                        Log.e("PlaylistFragment", "No playlists found for category: $categoryId")
                        Toast.makeText(context, "No playlists found for category: $categoryId", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("PlaylistFragment", "Error fetching playlists for $categoryId: ${response.message()}")
                    Toast.makeText(context, "Error fetching playlists for $categoryId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecommendedPlaylistsResponse>, t: Throwable) {
                Log.e("PlaylistFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
