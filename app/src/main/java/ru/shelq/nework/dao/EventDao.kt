package ru.shelq.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.PostEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Upsert
    fun save(event: EventEntity): Long

    @Query(
        """
         UPDATE EventEntity SET
                    likeOwnerIds = likeOwnerIds + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                    likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
                WHERE id = :id;
    """
    )
    fun likeById(id: Long)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    fun removeById(id: Long)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("SELECT authorAvatar FROM EventEntity where authorId = :authorId")
    suspend fun authorAvatar(authorId: Long): String
    @Query("UPDATE EventEntity SET read = 1 WHERE read = 0")
    suspend fun readNewEvents()
}