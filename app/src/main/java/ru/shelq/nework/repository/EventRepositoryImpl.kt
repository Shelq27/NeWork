package ru.shelq.nework.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.shelq.nework.dao.EventDao
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Post
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.PostEntity

class EventRepositoryImpl(private val dao: EventDao) : EventRepository {
    override fun getAll(): LiveData<List<Event>> = dao.getAll().map { events ->
        events.map(EventEntity::toDto)
    }


    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun save(event: Event) {
        dao.save(EventEntity.fromDto(event))
    }

}