package ru.shelq.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.shelq.nework.dto.Event
import ru.shelq.nework.enumer.EventType


@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = "",
    val authorAvatar: String? = "",
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val coords: CoordsEmbeddable?,
    val eventType: EventType,
    @TypeConverters
    val likeOwnerIds: List<Long>,
    val likedByMe: Boolean,
    @TypeConverters
    val speakerIds: List<Long>,
    @TypeConverters
    val participantsIds: List<Long>,
    val participatedByMe: Boolean,
    val read: Boolean = true,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val link: String? = null,
    val likes: Int = 0,
    val participants: Int = 0

) {
    fun toDto(): Event = Event(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        content,
        datetime,
        published,
        coords?.toDto(),
        eventType,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment?.toDto(),
        link,
        emptyMap(),
        likes = likes,
        participants = participants
    )

    companion object {
        fun fromDto(dto: Event): EventEntity = with(dto) {
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorJob,
                dto.authorAvatar,
                dto.content,
                dto.datetime,
                dto.published,
                coords = CoordsEmbeddable.fromDto(dto.coords),
                dto.type,
                dto.likeOwnerIds,
                dto.likedByMe,
                dto.speakerIds,
                dto.participantsIds,
                dto.participatedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                link = dto.link,
                likes = dto.likeOwnerIds.size,
                participants = dto.participantsIds.size

            )
        }
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)
