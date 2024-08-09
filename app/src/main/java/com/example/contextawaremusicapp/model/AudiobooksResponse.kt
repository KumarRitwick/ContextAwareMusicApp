data class AudiobooksResponse(
    val audiobooks: List<Audiobook>
)

data class Audiobook(
    val id: String,
    val name: String,
    val authors: List<Author>,
    val images: List<Image>,
    val description: String,
    val uri: String
)

data class Author(
    val name: String
)

data class Image(
    val url: String,
    val height: Int?,
    val width: Int?
)
