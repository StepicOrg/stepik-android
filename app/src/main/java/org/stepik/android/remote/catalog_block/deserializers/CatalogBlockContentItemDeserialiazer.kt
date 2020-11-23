package org.stepik.android.remote.catalog_block.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import org.stepik.android.domain.catalog_block.model.*
import java.lang.reflect.Type

class CatalogBlockContentItemDeserialiazer : JsonDeserializer<CatalogBlockItem> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CatalogBlockItem {
        val jsonObject = json.asJsonObject

        val kind = CatalogBlockItem.Kind.valueOf(jsonObject.get("kind").asString.toUpperCase())
        val contentField = jsonObject.get("content").asJsonArray
        val content = when (kind) {
            CatalogBlockItem.Kind.FULL_COURSE_LISTS ->
                context.deserialize<ArrayList<FullCourseListCatalogBlockContentItem>>(contentField, TypeToken.getParameterized(ArrayList::class.java, FullCourseListCatalogBlockContentItem::class.java).type)

            CatalogBlockItem.Kind.SIMPLE_COURSE_LISTS ->
                context.deserialize<ArrayList<SimpleCourseListCatalogBlockContentItem>>(contentField, TypeToken.getParameterized(ArrayList::class.java, SimpleCourseListCatalogBlockContentItem::class.java).type)

            CatalogBlockItem.Kind.SPECIALIZATIONS ->
                context.deserialize<ArrayList<SpecializationCatalogBlockContentItem>>(contentField, TypeToken.getParameterized(ArrayList::class.java, SpecializationCatalogBlockContentItem::class.java).type)

            CatalogBlockItem.Kind.AUTHORS, CatalogBlockItem.Kind.ORGANIZATIONS ->
                context.deserialize<ArrayList<AuthorCatalogBlockContentItem>>(contentField, TypeToken.getParameterized(ArrayList::class.java, AuthorCatalogBlockContentItem::class.java).type)

            else ->
                emptyList()
        }
        return CatalogBlockItem(
            id = jsonObject["id"].asLong,
            position = jsonObject["position"].asInt,
            title = jsonObject["title"].asString,
            description = jsonObject["description"].asString,
            language = jsonObject["language"].asString,
            kind = kind,
            appearance = jsonObject["appearance"].asString,
            isTitleVisible = jsonObject["is_title_visible"].asBoolean,
            content = content
        )
    }
}