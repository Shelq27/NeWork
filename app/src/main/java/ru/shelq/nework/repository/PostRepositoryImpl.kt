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
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.dao.PostRemoteKeyDao
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Attachment
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
import ru.shelq.nework.dto.User
import ru.shelq.nework.entity.PostEntity
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
open class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val auth: AppAuth,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 25),
        pagingSourceFactory = { postDao.pagingSource() },
        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao)
    ).flow.map { it.map(PostEntity::toDto) }

    override suspend fun getAll() {
        try {
            val response = apiService.getAllPosts()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(post: Post): Post {
        try {
            likeByIdLocal(post)
            val response = if (!post.likedByMe) {
                apiService.likePostById(post.id)
            } else {
                apiService.dislikePostById(post.id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            likeByIdLocal(post)
            throw NetworkError
        } catch (e: Exception) {
            likeByIdLocal(post)
            throw UnknownError
        }
    }

    override suspend fun likeByIdLocal(post: Post) {
        return if (post.likedByMe) {
            val list = post.likeOwnerIds.filter {
                it != auth.authState.value.id
            }
            postDao.likeById(post.id, list)
        } else {
            val list = post.likeOwnerIds.plus(auth.authState.value.id)
            postDao.likeById(post.id, list)
        }

    }


    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
        try {
            val response = apiService.deletePostById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = apiService.savePost(post.toPostApi())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(
        post: Post,
        upload: MediaUpload,
        attachmentType: AttachmentType
    ) {
        try {
            val media = upload(upload)
            val postWithAttachment = post.copy(attachment = Attachment(media.url, attachmentType))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
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

    override suspend fun getLikers(post: Post): List<User> {
        var likers = emptyList<User>()
        if (post.likeOwnerIds.isNotEmpty()) {
            post.likeOwnerIds.forEach {
                likers = likers.plus(getUser(it))
            }
        }
        return likers
    }


    override suspend fun getMentioned(post: Post): List<User> {
        var mentioned = emptyList<User>()
        if (post.mentionIds.isNotEmpty()) {
            post.mentionIds.forEach {
                mentioned = mentioned.plus(getUser(it))
            }
        }
        return mentioned
    }


    override suspend fun getPostById(postId: Long): Flow<Post?> {
        try {
            val response = apiService.getPostById(postId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val postEntity = PostEntity.fromDto(body)
            postDao.insert(postEntity)
            return postDao.getPost(postId).map { it?.toDto() }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerPost(id: Long): Flow<Int> = flow {
        while (true) {
            delay(60_000L)
            val response = apiService.getNewerPosts(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
            emit(body.size)
        }
    }.catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun latestReadPostId(): Long {
        return postDao.latestReadPostId() ?: 0L
    }

    override suspend fun readNewPosts() {
        postDao.readNewPosts()
    }

    override fun setUser(userId: Long) {
    }


}
