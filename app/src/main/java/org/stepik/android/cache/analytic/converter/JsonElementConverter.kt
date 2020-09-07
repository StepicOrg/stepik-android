package org.stepik.android.cache.analytic.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonElement

class JsonElementConverter {
    private val gson = Gson()

    @TypeConverter
    fun stringToJsonElement(value: String?): JsonElement =
        gson.toJsonTree(value)

    @TypeConverter
    fun jsonElementToString(jsonElement: JsonElement?): String =
        jsonElement?.toString() ?: ""
}