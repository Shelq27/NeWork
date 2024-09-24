package ru.shelq.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Post

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    suspend fun likeById(post: Post): Post
    suspend fun likeByIdLocal(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun getPostById(postId: Long): Post
    fun getNewerPost(id: Long): Flow<Int>
    suspend fun latestReadPostId(): Long
    suspend fun readNewPosts()


}
