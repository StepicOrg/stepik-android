package org.stepik.android.cache.catalog_block.mapper

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem

class CatalogBlockContentSerializer {
    private val gson = Gson()
    private val jsonParser = JsonParser()

    fun mapToLocalEntity(kind: String, content: CatalogBlockContent?): String? {
        val contentField = gson.toJsonTree(content).asJsonObject["content"]

        val contentJson = if (contentField is JsonObject) {
            JsonArray(1).apply { add(contentField) }
        } else {
            contentField
        }

        val localEntity = JsonObject()
        localEntity.addProperty("kind", kind)
        localEntity.add("content", contentJson)
        return localEntity.toString()
    }

    fun mapToDomainEntity(value: String?): CatalogBlockContent {
        val parsed = jsonParser.parse(value) as JsonObject
        val kind = parsed.remove("kind").asString
        val contentField = parsed["content"]
        return when (kind) {
            CatalogBlockContent.FULL_COURSE_LISTS ->
                CatalogBlockContent.FullCourseList(gson.fromJson<ArrayList<StandardCatalogBlockContentItem>>(contentField, TypeToken.getParameterized(ArrayList::class.java, StandardCatalogBlockContentItem::class.java).type).first())

            CatalogBlockContent.SIMPLE_COURSE_LISTS ->
                CatalogBlockContent.SimpleCourseList(gson.fromJson(contentField, TypeToken.getParameterized(ArrayList::class.java, StandardCatalogBlockContentItem::class.java).type))

            CatalogBlockContent.AUTHORS ->
                CatalogBlockContent.AuthorCourseList(gson.fromJson(contentField, TypeToken.getParameterized(ArrayList::class.java, AuthorCatalogBlockContentItem::class.java).type))

            else ->
                CatalogBlockContent.UnsupportedList
        }
    }
}