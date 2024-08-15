package ru.shelq.nework.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import ru.shelq.nework.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<EventEntity>>

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

    @Query("DELETE FROM EVENTENTITY WHERE id = :id")
    fun removeById(id: Long)
}