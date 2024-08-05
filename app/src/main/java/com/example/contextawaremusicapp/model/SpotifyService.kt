package com.example.contextawaremusicapp.model

import AudiobooksResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyService {
    @GET("v1/me")
    fun getCurrentUser(@Header("Authorization") authHeader: String): Call<UserResponse>

    @GET("v1/users/{user_id}/playlists")
    fun getUserPlaylists(
        @Header("Authorization") authHeader: String,
        @Path("user_id") userId: String
    ): Call<PlaylistsResponse>

    @GET("v1/tracks")
    fun getTracks(
        @Header("Authorization") authHeader: String,
        @Query("ids") trackIds: String
    ): Call<MultipleTracksResponse>

    @FormUrlEncoded
    @POST("api/token")
    fun refreshToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Call<RefreshTokenResponse>

    @GET("v1/playlists/{playlist_id}/tracks")
    fun getPlaylistTracks(
        @Header("Authorization") authHeader: String,
        @Path("playlist_id") playlistId: String
    ): Call<TracksResponse>

    @GET("v1/playlists/{playlist_id}")
    fun getPlaylistDetails(
        @Header("Authorization") authHeader: String,
        @Path("playlist_id") playlistId: String
    ): Call<Playlist>

    @GET("v1/recommendations")
    fun getRecommendations(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int,
        @Query("seed_genres") seedGenres: String
    ): Call<RecommendationResponse>

    @GET("v1/browse/featured-playlists")
    fun getFeaturedPlaylists(
        @Header("Authorization") token: String
    ): Call<PlaylistsResponse>

    @GET("v1/audiobooks")
    fun getAudiobooks(
        @Header("Authorization") token: String
    ): Call<AudiobooksResponse>
}
