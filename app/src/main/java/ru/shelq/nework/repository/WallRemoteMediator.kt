package ru.shelq.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.shelq.nework.api.ApiService
import ru.shelq.nework.auth.AppAuth
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.dao.WallRemoteKeyDao
import ru.shelq.nework.db.AppDb
import ru.shelq.nework.entity.PostEntity
import ru.shelq.nework.entity.WallRemoteKeyEntity
import ru.shelq.nework.entity.toEntity
import ru.shelq.nework.error.ApiError
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class WallRemoteMediator @Inject constructor(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val auth: AppAuth,
    private val userId: Long
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    if (isMe()) {
                        service.getMyWallLatest(state.config.initialLoadSize)
                    } else {
                        service.getUserWallLatest(userId, state.config.initialLoadSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = wallRemoteKeyDao.max(userId) ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    if (isMe()) {
                        service.getMyWallAfter(id, state.config.pageSize)
                    } else {
                        service.getUserWallAfter(userId, id, state.config.pageSize)
                    }
                }

                LoadType.APPEND -> {
                    val id = wallRemoteKeyDao.min(userId) ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    if (isMe()) {
                        service.getMyWallBefore(id, state.config.pageSize)
                    } else {
                        service.getUserWallBefore(userId, id, state.config.pageSize)
                    }

                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        wallRemoteKeyDao.removeAll(userId)
                        wallRemoteKeyDao.insert(
                            listOf(
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id,
                                    userId
                                ),
                                WallRemoteKeyEntity(
                                    type = WallRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id,
                                    userId
                                ),
                            )
                        )
                        postDao.clear()
                    }

                    LoadType.PREPEND -> {
                        wallRemoteKeyDao.insert(
                            WallRemoteKeyEntity(
                                type = WallRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                                userId
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        wallRemoteKeyDao.insert(
                            WallRemoteKeyEntity(
                                type = WallRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                                userId
                            )
                        )
                    }
                }
                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    fun isMe(): Boolean {
        return userId == auth.authState.value.id
    }
}