package ru.shelq.nework.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Upsert
    suspend fun save(post: PostEntity): Long

    @Query(
        """
         UPDATE PostEntity SET
                    likeOwnerIds = likeOwnerIds + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                    likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    suspend fun likeById(id: Long)

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
}