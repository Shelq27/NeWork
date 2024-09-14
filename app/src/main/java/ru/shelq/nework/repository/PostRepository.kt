package ru.shelq.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun likeByPost(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun getPostById(postId: Long): Post
    fun getNewerPost(id: Long): Flow<Int>
    suspend fun readNewPosts()



}
