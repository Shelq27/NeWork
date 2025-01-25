package ru.shelq.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.entity.JobEntity

@Dao
interface JobDao {

    @Query("SELECT * FROM JobEntity WHERE userId = :userId ORDER BY id")
    fun getAll(userId: Long): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(users: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT * FROM JobEntity WHERE userId = :userId AND finish == null ORDER BY id DESC")
    suspend fun getLastJob(userId: Long): JobEntity
}