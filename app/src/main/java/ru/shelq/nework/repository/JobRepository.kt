package ru.shelq.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.Job

interface JobRepository {
    var data: Flow<List<Job>>
    fun setUser(userId: Long)
    suspend fun getAll()
    suspend fun removeById(job: Job)
    suspend fun save(job: Job)
    suspend fun saveLocal(job: Job)
}