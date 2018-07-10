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
        private const val UTC_ISO_FORMAT_SIMPLE = "yyyy-MM-dd'T'HH:mm:ss"

        private fun createDateFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val serializeDateFormat = createDateFormat(UTC_ISO_FORMAT)
    private val deserializeDateFormat = createDateFormat(UTC_ISO_FORMAT_SIMPLE)

    override fun serialize(date: Date, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(serializeDateFormat.format(date))


    override fun deserialize(jsonElement: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Date = try {
        deserializeDateFormat.parse(jsonElement.asString.take(UTC_ISO_FORMAT_SIMPLE.length))
    } catch (e: ParseException) {
        throw JsonParseException(e)
    }
}