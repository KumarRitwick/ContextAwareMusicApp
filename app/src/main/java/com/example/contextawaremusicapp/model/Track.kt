package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("album") val album: Album,
    @SerializedName("duration_ms") val durationMs: Int,
    @SerializedName("uri") val uri: String
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<Image>
)
