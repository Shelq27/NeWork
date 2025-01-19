package ru.shelq.nework.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.shelq.nework.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
class DaoModule {
    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao

    @Provides
    fun provideEventDao(db: AppDb): EventDao = db.eventDao

    @Provides
    fun provideEventRemoteKeyDao(db: AppDb): EventRemoteKeyDao = db.eventRemoteKeyDao

    @Provides
    fun provideUserDao(db: AppDb): UserDao = db.userDao

    @Provides
    fun provideWallRemoteKeyDao(db: AppDb): WallRemoteKeyDao = db.wallRemoteKeyDao


}