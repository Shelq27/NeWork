package ru.shelq.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity where authorId = :authorId ORDER BY id DESC")
    fun pagingSourceUserWall(authorId: Long): PagingSource<Int, PostEntity>


    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END,
        likeOwnerIds = :likeOwnerIds
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long, likeOwnerIds: List<Long>)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPost(id: Long): Flow<PostEntity?>

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("SELECT authorAvatar FROM PostEntity where authorId = :authorId")
    suspend fun authorAvatar(authorId: Long): String

    @Query("UPDATE PostEntity SET read = 1 WHERE read = 0")
    suspend fun readNewPosts()

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(id) FROM PostEntity where read = 1")
    suspend fun latestReadPostId(): Long?

    @Query("SELECT MAX(id) FROM PostEntity where read = 1 AND authorId = :authorId")
    suspend fun latestUserReadPostId(authorId: Long): Long?

    @Query("DELETE FROM PostEntity")
    suspend fun clear()
}