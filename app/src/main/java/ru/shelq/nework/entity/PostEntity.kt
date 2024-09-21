package ru.shelq.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.shelq.nework.dto.Attachment
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.dto.Coordinates
import ru.shelq.nework.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    @Embedded
    val coords: CoordsEmbeddable?,
    val link: String?,
    @TypeConverters
    val mentionIds: List<Long>,
    val mentionedMe: Boolean,
    @TypeConverters
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val read: Boolean = true,
    @Embedded()
    val attachment: AttachmentEmbeddable?,
    val likes: Int = 0
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = coords?.toDto(),
        link = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment?.toDto(),
        emptyMap(),
    )

    companion object {
        fun fromDto(dto: Post): PostEntity = with(dto) {
            PostEntity(
                id = id,
                authorId = authorId,
                author = author,
                authorJob = authorJob,
                authorAvatar = authorAvatar,
                content = content,
                published = published,
                coords = CoordsEmbeddable.fromDto(dto.coords),
                link = link,
                mentionIds = mentionIds,
                mentionedMe = mentionedMe,
                likeOwnerIds = likeOwnerIds,
                likedByMe = likedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                likes = dto.likeOwnerIds.size

            )
        }
    }

}

data class CoordsEmbeddable(
    var latitude: Double,
    var longitude: Double,
) {
    fun toDto() = Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates?) = dto?.let {
            CoordsEmbeddable(it.lat, it.long)
        }
    }
}

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)

