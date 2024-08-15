package ru.shelq.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.shelq.nework.dto.Post


@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val coords: String, // TODO
    val link: String,
    val mentionIds: List<Long>, //TODO
    val mentionedMe: Boolean, //TODO
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    val attachment: String, //TODO
    val users: String,//TODO
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = coords,
        link = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = attachment,
        users = users
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
                coords = coords,
                link = link,
                mentionIds = mentionIds,
                mentionedMe = mentionedMe,
                likeOwnerIds = likeOwnerIds,
                likedByMe = likedByMe,
                attachment = attachment,
                users = users
            )
        }
    }
}