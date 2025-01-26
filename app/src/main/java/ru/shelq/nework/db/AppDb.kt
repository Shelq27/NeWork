package ru.shelq.nework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.shelq.nework.dao.EventDao
import ru.shelq.nework.dao.EventRemoteKeyDao
import ru.shelq.nework.dao.JobDao
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.dao.PostRemoteKeyDao
import ru.shelq.nework.dao.UserDao
import ru.shelq.nework.dao.WallRemoteKeyDao
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.EventRemoteKeyEntity
import ru.shelq.nework.entity.JobEntity
import ru.shelq.nework.entity.PostEntity
import ru.shelq.nework.entity.PostRemoteKeyEntity
import ru.shelq.nework.entity.UserEntity
import ru.shelq.nework.entity.WallRemoteKeyEntity
import ru.shelq.nework.util.Converters

@Database(
    entities = [
        PostEntity::class,
        EventEntity::class,
        UserEntity::class,
        JobEntity::class,
        PostRemoteKeyEntity::class,
        EventRemoteKeyEntity::class,
        WallRemoteKeyEntity::class,
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao
    abstract val eventDao: EventDao
    abstract val userDao: UserDao
    abstract val jobDao: JobDao
    abstract val postRemoteKeyDao: PostRemoteKeyDao
    abstract val eventRemoteKeyDao: EventRemoteKeyDao
    abstract val wallRemoteKeyDao: WallRemoteKeyDao
    companion object {

        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .fallbackToDestructiveMigration()
                //  .allowMainThreadQueries()
                .build()
    }
}