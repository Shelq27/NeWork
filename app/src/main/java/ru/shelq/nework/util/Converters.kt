package ru.shelq.nework.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun listToString(list: List<Long>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToList(json: String?): List<Long?>? {
        return Gson().fromJson(json, object : TypeToken<List<Long?>?>() {}.type)
    }
}