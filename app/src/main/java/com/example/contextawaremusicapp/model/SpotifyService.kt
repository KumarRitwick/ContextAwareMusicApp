package com.example.contextawaremusicapp.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface SpotifyService {
    @GET("v1/me/playlists")
    fun getUserPlaylists(@Header("Authorization") authHeader: String): Call<PlaylistsResponse>
}


