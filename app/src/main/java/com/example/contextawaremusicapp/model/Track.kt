package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("duration_ms") val durationMs: Int
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
