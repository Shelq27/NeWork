package ru.shelq.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Jobs
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
import ru.shelq.nework.dto.User
import ru.shelq.nework.enumer.AttachmentType

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, attachmentType: AttachmentType)
    suspend fun likeById(post: Post): Post
    suspend fun likeByIdLocal(post: Post)
    suspend fun removeById(id: Long)
    suspend fun getPostById(postId: Long): Flow<Post?>
    suspend fun latestReadPostId(): Long
    suspend fun readNewPosts()
    suspend fun getUser(userId: Long): User
    suspend fun getLikers(post: Post): List<User>
    suspend fun getMentioned(post: Post): List<User>
    fun getNewerPost(id: Long): Flow<Int>
    fun setUser(userId: Long)
    suspend fun upload(upload: MediaUpload): Media



}
