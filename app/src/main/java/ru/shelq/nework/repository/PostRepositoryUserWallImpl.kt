package ru.shelq.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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
import ru.shelq.nework.dao.WallRemoteKeyDao
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
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
class PostRepositoryUserWallImpl @Inject constructor(
    private val appDb: AppDb,
    private val dao: PostDao,
    private val apiService: ApiService,
    private val auth: AppAuth,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
) : PostRepository {

    var userId: Long = 0

    override lateinit var data: Flow<PagingData<Post>>

    @OptIn(ExperimentalPagingApi::class)
    override fun setUser(userId: Long) {
        this.userId = userId
        data = Pager(
            config = PagingConfig(pageSize = 25),
            remoteMediator = WallRemoteMediator(
                apiService,
                appDb,
                dao,
                wallRemoteKeyDao,
                auth,
                userId
            ),
            pagingSourceFactory = {
                dao.pagingSourceUserWall(userId)
            }
        ).flow.map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }
    }




    override suspend fun getAll() {
        try {
            val response =
                if (isMyWall()) {
                    apiService.getMyWall()
                } else {
                    apiService.getUserWall(userId)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
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
                apiService.likeUserPostById(userId, post.id)
            } else {
                apiService.dislikeUserPostById(userId, post.id)
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
            dao.likeById(post.id, list)
        } else {
            val list = post.likeOwnerIds.plus(auth.authState.value.id)
            dao.likeById(post.id, list)
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
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
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun getPostById(postId: Long): Flow<Post?> {
        try {
            val response = apiService.getPostById(postId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val postEntity = PostEntity.fromDto(body)
            dao.insert(postEntity)
            return dao.getPost(postId).map { it?.toDto() }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerPost(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response =
                if (isMyWall()) {
                    apiService.getMyWallNewer(id)
                } else {
                    apiService.getUserWallNewer(userId, id)
                }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //записываем новые посты с признаком read = false
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun latestReadPostId(): Long {
        return dao.latestUserReadPostId(userId) ?: 0L
    }

    override suspend fun readNewPosts() {
    }
    override suspend fun saveWithAttachment(
        post: Post,
        upload: MediaUpload,
        attachmentType: AttachmentType
    ) {
        TODO("Not yet implemented")

    }
    override suspend fun upload(upload: MediaUpload): Media {
        TODO("Not yet implemented")
    }


    fun isMyWall(): Boolean {
        return userId == auth.authState.value.id
    }
}