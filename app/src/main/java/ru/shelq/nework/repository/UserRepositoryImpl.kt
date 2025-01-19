package ru.shelq.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.dao.UserDao
import ru.shelq.nework.entity.UserEntity
import ru.shelq.nework.entity.toDto
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import ru.shelq.nework.error.NetworkError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val apiService: ApiService,

    ): UserRepository{

    override val data = dao.getAll()
        .map(List<UserEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getAll() {
        try {
            val response = apiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        }
    }


}