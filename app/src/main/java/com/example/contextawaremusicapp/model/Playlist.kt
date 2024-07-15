package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("url") val url: String
)

data class Playlist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("images") val images: List<Image>
)

data class PlaylistsResponse(
    @SerializedName("items") val playlists: List<Playlist>
)
