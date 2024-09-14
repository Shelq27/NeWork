package ru.shelq.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.dao.EventDao
import ru.shelq.nework.dto.Event
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.toDto
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.AppError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError
import java.io.IOException

class EventRepositoryImpl(
    private val eventDao: EventDao,
) : EventRepository {
    override val data = eventDao.getAll()
        .map(List<EventEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = ApiService.service.getAllEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeByEvent(event: Event) {
        eventDao.likeById(event.id)
        try {
            val response = if (event.likedByMe) {
                ApiService.service.dislikeEventById(event.id)
            } else {
                ApiService.service.likeEventById(event.id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            eventDao.likeById(event.id)
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        eventDao.removeById(id)
        try {
            val response = ApiService.service.deleteEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(event: Event) {
        try {
            val response = ApiService.service.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getEventById(eventId: Long): Event {
        try {
            val response = ApiService.service.getEventById(eventId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val eventEntity = EventEntity.fromDto(body)
            eventDao.insert(eventEntity)
            return eventEntity.toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerEvent(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = ApiService.service.getNewerEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(body.toEntity())
            emit(body.size)
        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun readNewEvents() {
        eventDao.readNewEvents()
    }

}
