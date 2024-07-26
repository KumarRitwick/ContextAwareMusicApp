package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class TracksResponse(
    @SerializedName("tracks") val tracks: List<Track>
)
