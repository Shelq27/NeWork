package ru.shelq.nework.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.dto.Post

class PostRepositoryImpl : PostRepository {

    override fun getAll(): List<Post> {
        return ApiService.service.getAllPosts()
            .execute()
            .let {
                it.body() ?: throw RuntimeException("body is null")
            }
    }

    override fun getAllAsync(callback: PostRepository.GetPostsCallback) {
        ApiService.service.getAllPosts()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (response.isSuccessful) {
                        callback.onError(Exception(response.errorBody()?.string()))
                        return
                    }
                    val body = response.body() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(body)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun likeById(id: Long) {
        likeById(id)
    }

    override fun removeById(id: Long) {
        ApiService.service.dislikePostById(id)
            .execute()
    }

    override fun save(post: Post) {
        ApiService.service.savePost(post)
            .execute()

    }
}
