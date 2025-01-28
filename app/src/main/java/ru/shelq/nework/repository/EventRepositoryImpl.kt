package ru.shelq.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dao.EventDao
import ru.shelq.nework.dao.EventRemoteKeyDao
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Attachment
import ru.shelq.nework.dto.Event
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.User
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.enumer.AttachmentType
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.AppError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val auth: AppAuth,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb
) : EventRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 25),
        pagingSourceFactory = { eventDao.pagingSource() },
        remoteMediator = EventRemoteMediator(apiService, appDb, eventDao, eventRemoteKeyDao)
    ).flow.map { it.map(EventEntity::toDto) }

    override suspend fun getAll() {
        try {
            val response = apiService.getAllEvents()
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

    override suspend fun likeById(event: Event): Event {
        try {
            likeByIdLocal(event)
            val response = if (!event.likedByMe) {
                apiService.likeEventById(event.id)
            } else {
                apiService.dislikeEventById(event.id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            likeByIdLocal(event)
            throw NetworkError
        } catch (e: Exception) {
            likeByIdLocal(event)
            throw UnknownError
        }
    }

    override suspend fun likeByIdLocal(event: Event) {
        return if (event.likedByMe) {
            val list = event.likeOwnerIds.filter {
                it != auth.authState.value.id
            }
            eventDao.likeById(event.id, list)
        } else {
            val list = event.likeOwnerIds.plus(auth.authState.value.id)
            eventDao.likeById(event.id, list)
        }

    }

    override suspend fun removeById(id: Long) {
        eventDao.removeById(id)
        try {
            val response = apiService.deleteEventById(id)
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
            val response = apiService.saveEvent(event)
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

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(
        event: Event,
        upload: MediaUpload,
        attachmentType: AttachmentType
    ) {
        try {
            val media = upload(upload)
            val eventWithAttachment = event.copy(attachment = Attachment(media.url, attachmentType))
            save(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getEventById(eventId: Long): Flow<Event> {
        try {
            val response = apiService.getEventById(eventId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val eventEntity = EventEntity.fromDto(body)
            eventDao.insert(eventEntity)
            return eventDao.getEvent(eventId).map { it?.toDto()!! }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerEvent(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewerEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(body.toEntity())
            emit(body.size)
        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun latestReadEventId(): Long {
        return eventDao.latestReadEventId() ?: 0L
    }

    override suspend fun getUser(userId: Long): User {
        try {
            val response = apiService.getUserById(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    override suspend fun getLikers(event: Event): List<User> {
        var likers = emptyList<User>()
        if (event.likeOwnerIds.isNotEmpty()) {
            event.likeOwnerIds.forEach {
                likers = likers.plus(getUser(it))
            }
        }
        return likers
    }

    override suspend fun getSpeakers(event: Event): List<User> {
        var speakers = emptyList<User>()
        if (event.speakerIds.isNotEmpty()) {
            event.speakerIds.forEach {
                speakers = speakers.plus(getUser(it))
            }
        }
        return speakers
    }

    override suspend fun getParticipants(event: Event): List<User> {
        var participants = emptyList<User>()
        if (event.participantsIds.isNotEmpty()) {
            event.participantsIds.forEach {
                participants = participants.plus(getUser(it))
            }
        }
        return participants
    }

    override suspend fun readNewEvents() {
        eventDao.readNewEvents()
    }

}
