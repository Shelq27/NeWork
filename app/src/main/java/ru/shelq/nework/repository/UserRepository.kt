package ru.shelq.nework.repository

import kotlinx.coroutines.flow.Flow
import ru.shelq.nework.dto.User

interface UserRepository {
    val data: Flow<List<User>>
    suspend fun getAll()
}