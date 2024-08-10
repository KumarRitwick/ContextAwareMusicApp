import com.google.gson.annotations.SerializedName
import com.example.contextawaremusicapp.model.Track

data class SpotifyQueueResponse(
    @SerializedName("currently_playing") val currentlyPlaying: Track,
    @SerializedName("queue") val queue: List<Track>
)

data class Track(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("album") val album: Album,
    @SerializedName("duration_ms") val durationMs: Int,
    @SerializedName("uri") val uri: String
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("uri") val uri: String
)

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<QueueImage>,
    @SerializedName("uri") val uri: String
)

data class QueueImage(
    @SerializedName("url") val url: String
)

