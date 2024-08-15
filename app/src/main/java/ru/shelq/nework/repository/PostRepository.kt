package ru.shelq.nework.repository

import ru.shelq.nework.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun getAllAsync(callback: GetPostsCallback)
    fun likeById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)

    interface GetPostsCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(throwable: Throwable)
    }
}
