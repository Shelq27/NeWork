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
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.dao.PostRemoteKeyDao
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Post
import ru.shelq.nework.entity.PostEntity
import ru.shelq.nework.entity.toDto
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.AppError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data = Pager(
        config = PagingConfig(pageSize = 25, enablePlaceholders = false),
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

    override suspend fun likeByPost(post: Post) {
        return if (post.likedByMe) {
            val list = post.likeOwnerIds.filter {
                it != AppAuth.getInstance().authState.value.id
            }
            apiService.dislikePostById(post.id)
            postDao.likeById(post.id, list)
        } else {
            val list = post.likeOwnerIds.plus(AppAuth.getInstance().authState.value.id)
            apiService.likePostById(post.id)
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
            val response = apiService.savePost(post)
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

    override suspend fun getPostById(postId: Long): Post {
        try {
            val response = apiService.getPostById(postId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val postEntity = PostEntity.fromDto(body)
            postDao.insert(postEntity)
            return postEntity.toDto()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    override fun getNewerPost(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
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
}
