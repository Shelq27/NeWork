package ru.shelq.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.shelq.nework.entity.WallRemoteKeyEntity

@Dao
interface WallRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM WallRemoteKeyEntity WHERE userId = :userId")
    suspend fun isEmpty(userId: Long): Boolean

    @Query("SELECT MAX(id) FROM WallRemoteKeyEntity WHERE userId = :userId")
    suspend fun max(userId: Long): Long?

    @Query("SELECT MIN(id) FROM WallRemoteKeyEntity WHERE userId = :userId")
    suspend fun min(userId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: WallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<WallRemoteKeyEntity>)

    @Query("DELETE FROM WallRemoteKeyEntity WHERE userId = :userId")
    suspend fun removeAll(userId: Long)
}