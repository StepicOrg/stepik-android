package org.stepik.android.remote.catalog_block.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonObject
import org.stepik.android.cache.catalog_block.mapper.CatalogBlockContentSerializer
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import java.lang.reflect.Type

class CatalogBlockContentItemDeserialiazer : JsonDeserializer<CatalogBlockItem> {
    private val catalogBlockContentSerializer = CatalogBlockContentSerializer()
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CatalogBlockItem {
        val jsonObject = json.asJsonObject
        val toMapperJson = JsonObject()

        val kind = jsonObject.get("kind")
        val contentField = jsonObject.get("content")
        toMapperJson.add("kind", kind)
        toMapperJson.add("content", contentField)

        val content = catalogBlockContentSerializer.mapToDomainEntity(toMapperJson.toString())

        return CatalogBlockItem(
            id = jsonObject["id"].asLong,
            position = jsonObject["position"].asInt,
            title = jsonObject["title"].asString,
            description = jsonObject["description"].asString,
            language = jsonObject["language"].asString,
            appearance = jsonObject["appearance"].asString,
            isTitleVisible = jsonObject["is_title_visible"].asBoolean,
            content = content
        )
    }
}