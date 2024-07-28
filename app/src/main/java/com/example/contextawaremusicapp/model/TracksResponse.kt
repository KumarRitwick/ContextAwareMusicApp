package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class TracksResponse(
    @SerializedName("items") val items: List<TrackItem>
)

data class TrackItem(
    @SerializedName("track") val track: Track
)
