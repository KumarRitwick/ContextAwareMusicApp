package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class MultipleTracksResponse(
    @SerializedName("tracks") val tracks: List<Track>
)
