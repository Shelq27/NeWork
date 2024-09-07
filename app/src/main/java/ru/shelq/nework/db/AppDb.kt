package ru.shelq.nework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.shelq.nework.dao.EventDao
import ru.shelq.nework.dao.PostDao
import ru.shelq.nework.entity.EventEntity
import ru.shelq.nework.entity.PostEntity
import ru.shelq.nework.util.Converters

@Database(entities = [PostEntity::class, EventEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao
    abstract val eventDao: EventDao

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
                .allowMainThreadQueries()
                .build()
    }
}