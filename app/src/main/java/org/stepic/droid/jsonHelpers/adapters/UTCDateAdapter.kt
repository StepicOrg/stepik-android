package org.stepic.droid.jsonHelpers.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.jetbrains.annotations.Contract
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UTCDateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    companion object {
        private const val UTC_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val UTC_ISO_FORMAT_SIMPLE = "yyyy-MM-dd'T'HH:mm:ss"
        private const val UTC_ISO_FORMAT_SIMPLE_LENGTH = UTC_ISO_FORMAT_SIMPLE.length - 2 // exclude 2 single quotes around T

        private fun createDateFormat(pattern: String): SimpleDateFormat =
            SimpleDateFormat(pattern, Locale.getDefault())
                .apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
    }

    private val serializeDateFormat = createDateFormat(UTC_ISO_FORMAT)
    private val deserializeDateFormat = createDateFormat(UTC_ISO_FORMAT_SIMPLE)

    override fun serialize(date: Date, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        synchronized(serializeDateFormat) {
            JsonPrimitive(serializeDateFormat.format(date))
        }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Date =
        try {
            synchronized(deserializeDateFormat) {
                deserializeDateFormat.parse(jsonElement.asString.take(UTC_ISO_FORMAT_SIMPLE_LENGTH))
            }
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }

    @Contract("null -> null; !null -> !null", pure = true)
    fun dateToString(date: Date?): String? =
        date?.let { serialize(date, null, null).asString }

    @Contract("null -> null; !null -> !null", pure = true)
    fun stringToDate(date: String?): Date? =
        date?.let { deserialize(JsonPrimitive(date), null, null) }
}