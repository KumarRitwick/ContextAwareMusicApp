package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class RecommendationsResponse(
    @SerializedName("tracks") val tracks: List<Track>
)


