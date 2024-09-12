package ru.shelq.nework.repository

import androidx.lifecycle.LiveData
import ru.shelq.nework.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun likeByPost(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun getPostById(postId: Long): Post


}
