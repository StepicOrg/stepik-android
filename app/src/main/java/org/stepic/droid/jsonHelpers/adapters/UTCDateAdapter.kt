package org.stepic.droid.jsonHelpers.adapters

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.JsonParseException
import java.text.ParseException


class UTCDateAdapter: JsonSerializer<Date>, JsonDeserializer<Date> {
    companion object {
        private const val UTC_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    private val dateFormat = SimpleDateFormat(UTC_ISO_FORMAT, Locale.getDefault()).also {
        it.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun serialize(date: Date, typeOfSrc: Type, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(dateFormat.format(date))


    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date = try {
        dateFormat.parse(jsonElement.asString)
    } catch (e: ParseException) {
        throw JsonParseException(e)
    }
}