package ru.shelq.nework.repository

import androidx.lifecycle.map
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.dto.Post
import ru.shelq.nework.entity.PostEntity
import ru.shelq.nework.entity.toDto
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError
import java.io.IOException

class PostRepositoryImpl(
    private val postDao: PostDao,
) : PostRepository {
    override val data = postDao.getAll().map(List<PostEntity>::toDto)


    override suspend fun getAll() {
        try {
            val response = ApiService.service.getAllPosts()
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
        postDao.likeById(post.id)
        try {
            val response = if (post.likedByMe) {
                ApiService.service.dislikePostById(post.id)
            } else {
                ApiService.service.likePostById(post.id)
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            postDao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
        try {
            val response = ApiService.service.deletePostById(id)
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
            val response = ApiService.service.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            UnknownError
        }
    }

    override suspend fun getPostById(postId: Long): Post {
        try {
            val response = ApiService.service.getPostById(postId)
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
            UnknownError
        }
        // Уточнить !!!
        return TODO("Provide the return value")
    }

}
