data class RecommendedPlaylistsResponse(
    val playlists: Playlists
)

data class Playlists(
    val href: String,
    val items: List<com.example.contextawaremusicapp.model.Playlist>,
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total: Int
)

data class Playlist(
    val collaborative: Boolean,
    val description: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val owner: Owner,
    val primary_color: String?,
    val public: Boolean,
    val snapshot_id: String,
    val tracks: Tracks,
    val type: String,
    val uri: String,
    val category: String
)

data class ExternalUrls(
    val spotify: String
)


data class Owner(
    val display_name: String,
    val external_urls: ExternalUrls,
    val href: String,
    val id: String,
    val type: String,
    val uri: String
)

data class Tracks(
    val href: String,
    val total: Int
)
