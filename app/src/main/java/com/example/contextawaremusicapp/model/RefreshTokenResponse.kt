package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String
)
