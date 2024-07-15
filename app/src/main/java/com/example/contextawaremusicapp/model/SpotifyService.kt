package com.example.contextawaremusicapp.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface SpotifyService {
    @GET("v1/me")
    fun getCurrentUser(@Header("Authorization") authHeader: String): Call<UserResponse>

    @GET("v1/users/{user_id}/playlists")
    fun getUserPlaylists(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: String
    ): Call<PlaylistsResponse>
}
