package com.example.contextawaremusicapp.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("album") val album: Album,
    @SerializedName("duration_ms") val durationMs: Int
) : Parcelable

@Parcelize
data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
) : Parcelable

@Parcelize
data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<Image>
) : Parcelable

@Parcelize
data class Image(
    @SerializedName("url") val url: String
) : Parcelable
