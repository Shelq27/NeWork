package ru.shelq.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Jobs

interface JobRepository {
    val data: Flow<List<Jobs>>
    fun setUser(userId: Long)
    suspend fun getAll()
    suspend fun removeById(jobs: Jobs)
    suspend fun save(jobs: Jobs)
    suspend fun saveLocal(jobs: Jobs)
}