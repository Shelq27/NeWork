package ru.shelq.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import ru.shelq.nework.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Upsert
    suspend fun save(event: EventEntity): Long
    @Query(
        """
        UPDATE EventEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END,
        likeOwnerIds = :likeOwnerIds
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long, likeOwnerIds: List<Long>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("SELECT authorAvatar FROM EventEntity where authorId = :authorId")
    suspend fun authorAvatar(authorId: Long): String

    @Query("UPDATE EventEntity SET read = 1 WHERE read = 0")
    suspend fun readNewEvents()

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MAX(id) FROM EventEntity where read = 1")
    suspend fun latestReadEventId(): Long?

    @Query("DELETE FROM EventEntity")
    suspend fun clear()
}