package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("tracks") val tracks: List<Track>
)
