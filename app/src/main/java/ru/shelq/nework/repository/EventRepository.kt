package ru.shelq.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
import ru.shelq.nework.enumer.AttachmentType

interface EventRepository {
    val data: Flow<PagingData<Event>>
    suspend fun getAll()
    suspend fun likeById(event: Event): Event
    suspend fun likeByIdLocal(event: Event)
    suspend fun removeById(id: Long)
    suspend fun save(event: Event)
    suspend fun saveWithAttachment(event: Event, upload: MediaUpload, attachmentType: AttachmentType)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun getEventById(eventId: Long): Event
    fun getNewerEvent(id: Long): Flow<Int>
    suspend fun readNewEvents()
    suspend fun latestReadEventId(): Long
}