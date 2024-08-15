package ru.shelq.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.shelq.nework.dto.Event

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: String, // TODO
    val type: String,
    val likeOwnerIds: Long,
    val likedByMe: Boolean,
    val participantsIds: Long, //TODO
    val participatedByMe: Boolean, //TODO
    val attachment: String, //TODO
    val link: String,
    val users: String,//TODO

) {
    fun toDto(): Event = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        datetime = datetime,
        published = published,
        coords = coords,
        type = type,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment,
        link = link,
        users = users
    )

    companion object {
        fun fromDto(dto: Event): EventEntity = with(dto) {
            EventEntity(
                id = id,
                authorId = authorId,
                author = author,
                authorJob = authorJob,
                authorAvatar = authorAvatar,
                content = content,
                datetime = datetime,
                published = published,
                coords = coords,
                type = type,
                likeOwnerIds = likeOwnerIds,
                likedByMe = likedByMe,
                participantsIds = participantsIds,
                participatedByMe = participatedByMe,
                attachment = attachment,
                link = link,
                users = users
            )
        }
    }
}