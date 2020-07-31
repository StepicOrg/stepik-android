package org.stepik.android.cache.analytic.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonElement

class JsonElementConverter {
    @TypeConverter
    fun stringToJsonElement(value: String?): JsonElement =
        Gson().toJsonTree(value)

    @TypeConverter
    fun jsonElementToString(jsonElement: JsonElement?): String =
        jsonElement?.toString() ?: ""
}