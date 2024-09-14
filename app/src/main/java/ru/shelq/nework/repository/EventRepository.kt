package ru.shelq.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Event

interface EventRepository {
    val data: Flow<List<Event>>
    suspend fun getAll()
    suspend fun likeByEvent(event: Event)
    suspend fun removeById(id: Long)
    suspend fun save(event: Event)
    suspend fun getEventById(eventId: Long): Event
    fun getNewerEvent(id: Long): Flow<Int>
    suspend fun readNewEvents()
}