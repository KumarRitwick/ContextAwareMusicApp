package com.example.contextawaremusicapp.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("country") val country: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("email") val email: String,
    @SerializedName("explicit_content") val explicitContent: ExplicitContent,
    @SerializedName("external_urls") val externalUrls: ExternalUrls,
    @SerializedName("followers") val followers: Followers,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: List<Image>,
    @SerializedName("product") val product: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)

data class ExplicitContent(
    @SerializedName("filter_enabled") val filterEnabled: Boolean,
    @SerializedName("filter_locked") val filterLocked: Boolean
)

data class ExternalUrls(
    @SerializedName("spotify") val spotify: String
)

data class Followers(
    @SerializedName("href") val href: String?,
    @SerializedName("total") val total: Int
)
