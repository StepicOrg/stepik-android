package org.stepik.android.remote.catalog_block.deserializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.CatalogBlockItem
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem
import java.lang.reflect.Type

class CatalogBlockContentItemDeserialiazer : JsonDeserializer<CatalogBlockItem> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CatalogBlockItem {
        val jsonObject = json.asJsonObject

        val kind = jsonObject.get("kind").asString
        val contentField = jsonObject.get("content").asJsonArray
        val content = when (kind) {
            CatalogBlockContent.FULL_COURSE_LISTS ->
                CatalogBlockContent.FullCourseList(
                    context.deserialize<StandardCatalogBlockContentItem>(
                        contentField.first(),
                        StandardCatalogBlockContentItem::class.java
                    )
                )

            CatalogBlockContent.SIMPLE_COURSE_LISTS ->
                CatalogBlockContent.SimpleCourseList(
                    context.deserialize<ArrayList<StandardCatalogBlockContentItem>>(
                        contentField,
                        TypeToken.getParameterized(ArrayList::class.java, StandardCatalogBlockContentItem::class.java).type
                    )
                )

            CatalogBlockContent.AUTHORS ->
                CatalogBlockContent.AuthorCourseList(
                    context.deserialize<ArrayList<AuthorCatalogBlockContentItem>>(
                        contentField,
                        TypeToken.getParameterized(ArrayList::class.java, AuthorCatalogBlockContentItem::class.java).type
                    )
                )

            else ->
                CatalogBlockContent.UnsupportedList
        }
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