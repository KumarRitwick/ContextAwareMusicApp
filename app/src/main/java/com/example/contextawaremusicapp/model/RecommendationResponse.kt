package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(
    @SerializedName("seeds") val seeds: List<Seed>,
    @SerializedName("tracks") val tracks: List<Track>
)

data class Seed(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("href") val href: String,
    @SerializedName("initialPoolSize") val initialPoolSize: Int,
    @SerializedName("afterFilteringSize") val afterFilteringSize: Int,
    @SerializedName("afterRelinkingSize") val afterRelinkingSize: Int
)
