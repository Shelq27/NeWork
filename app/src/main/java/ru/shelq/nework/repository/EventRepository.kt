package ru.shelq.nework.repository

import androidx.lifecycle.LiveData
import ru.shelq.nework.dto.Event

interface EventRepository {
    fun getAll(): LiveData<List<Event>>
    fun likeById(id: Long)
    fun removeById(id: Long)
    fun save(event: Event)
}