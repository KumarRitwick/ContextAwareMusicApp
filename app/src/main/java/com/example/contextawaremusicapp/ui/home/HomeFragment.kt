package com.example.contextawaremusicapp.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.contextawaremusicapp.R
import com.example.contextawaremusicapp.controller.TrackAdapter
import com.example.contextawaremusicapp.model.SpotifyApi
import com.example.contextawaremusicapp.model.TracksResponse
import com.example.contextawaremusicapp.model.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.home_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TrackAdapter(listOf())
        recyclerView.adapter = adapter

        fetchTracks()

        return view
    }

    private fun fetchTracks() {
        val accessToken = getAccessToken(requireContext())
        val trackIds = "3n3Ppam7vgaVa1iaRUc9Lp,3twNvmDtFQtAd5gMKedhLD,7C81Nw1PIVPOn7HeVYOIrR"
        SpotifyApi.service.getTracks(
            "Bearer $accessToken",
            trackIds
        ).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.tracks ?: emptyList()
                    if (tracks.isNotEmpty()) {
                        adapter.updateTracks(tracks)
                    } else {
                        Toast.makeText(context, "No tracks found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error fetching tracks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                Toast.makeText(context, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAccessToken(context: Context): String {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "SpotifyCredential",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("ACCESS_TOKEN", "") ?: ""
    }
}
