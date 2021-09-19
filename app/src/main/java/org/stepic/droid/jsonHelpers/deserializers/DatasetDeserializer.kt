package org.stepic.droid.jsonHelpers.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import org.jsoup.Jsoup
import org.stepik.android.model.attempts.Component

import org.stepik.android.model.attempts.Pair
import org.stepik.android.model.attempts.Dataset
import org.stepik.android.model.attempts.DatasetWrapper

import java.lang.reflect.Type

class DatasetDeserializer : JsonDeserializer<DatasetWrapper> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DatasetWrapper {
        return if (json !is JsonObject) {
            try {
                val o = context.deserialize<String>(json, String::class.java)
                val dataset = Dataset(someStringValueFromServer = o)
                DatasetWrapper(dataset)
            } catch (e: Exception) {
                //if it is primitive, but not string.
                DatasetWrapper()
            }

        } else {
            val dataset = Dataset(
                options = context.deserialize<List<String>?>(json.getAsJsonArray("components"), TypeToken.getParameterized(ArrayList::class.java, String::class.java).type),
                someStringValueFromServer = null,
                pairs = context.deserialize<List<Pair>?>(json.getAsJsonArray("pairs"), TypeToken.getParameterized(ArrayList::class.java, Pair::class.java).type),
                rows = processStringList("rows", json),
                columns = processStringList("columns", json),
                description = context.deserialize<String?>(json.get("description"), String::class.java),
                components = context.deserialize<List<Component>?>(json.getAsJsonArray("components"), TypeToken.getParameterized(ArrayList::class.java, Component::class.java).type),
                isMultipleChoice = if (json.has("is_multiple_choice")) context.deserialize(json.get("is_multiple_choice"), Boolean::class.java) else false,
                isCheckbox = if (json.has("is_checkbox")) context.deserialize(json.get("is_checkbox"), Boolean::class.java) else false,
                isHtmlEnabled = if (json.has("is_html_enabled") )context.deserialize(json.get("is_html_enabled"), Boolean::class.java) else false,
            )
            DatasetWrapper(dataset)
        }
    }

    private fun processStringList(key: String, jsonObject: JsonObject): List<String>? =
        if (jsonObject.has(key)) {
            jsonObject
                .get(key)
                .takeIf { !it.isJsonNull }
                ?.asJsonArray
                ?.map { jsonElement ->
                    val element = jsonElement.asString
                   Jsoup.parseBodyFragment(element).body().html()
                }
        } else {
            null
        }
}
