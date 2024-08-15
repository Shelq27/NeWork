package ru.shelq.nework.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.shelq.nework.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): List<PostEntity>

    @Upsert
    fun save(post: PostEntity): Long

    @Query(
        """
         UPDATE PostEntity SET
                    likeOwnerIds = likeOwnerIds + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                    likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Long)
}