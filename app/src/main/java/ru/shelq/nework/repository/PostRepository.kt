package ru.shelq.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Media
import ru.shelq.nework.dto.MediaUpload
import ru.shelq.nework.dto.Post
import ru.shelq.nework.enumer.AttachmentType

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getAll()
    suspend fun likeById(post: Post): Post
    suspend fun likeByIdLocal(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload, attachmentType: AttachmentType)
    suspend fun getPostById(postId: Long): Flow<Post?>
    fun getNewerPost(id: Long): Flow<Int>
    suspend fun latestReadPostId(): Long
    suspend fun readNewPosts()
    fun setUser(userId: Long)
    suspend fun upload(upload: MediaUpload): Media


}
