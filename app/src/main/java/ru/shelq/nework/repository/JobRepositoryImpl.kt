package ru.shelq.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.dao.JobDao
import ru.shelq.nework.dto.Job
import ru.shelq.nework.entity.JobEntity
import ru.shelq.nework.entity.toDto
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.NetworkError
import ru.shelq.nework.error.UnknownError

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val dao: JobDao,
    private val apiService: ApiService,
): JobRepository {

    override lateinit var data: Flow<List<Job>>
    var userId: Long = 0

    override fun setUser(userId: Long) {
        this.userId = userId
        data = dao.getAll(userId)
            .map(List<JobEntity>::toDto)
            .flowOn(Dispatchers.Default)
    }

    override suspend fun getAll(){
        try {
            val response = apiService.getUserJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity(userId))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
           throw  UnknownError
        }
    }

    override suspend fun removeById(job: Job) {
        val jobRemoved = job.copy()
        try {
            dao.removeById(job.id)
            val response = apiService.removeJobById(job.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            saveLocal(jobRemoved)
            throw NetworkError
        } catch (e: Exception) {
            saveLocal(jobRemoved)
            throw UnknownError
        }
    }

    override suspend fun save(job: Job) {
        try {
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(JobEntity.fromDto(body, userId))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveLocal(job: Job) {
        dao.insert(JobEntity.fromDto(job, userId))
    }
}