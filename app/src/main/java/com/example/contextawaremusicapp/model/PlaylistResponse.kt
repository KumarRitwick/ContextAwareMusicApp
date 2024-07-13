package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class PlaylistsResponse(
    @SerializedName("items") val playlists: List<Playlist>
)
