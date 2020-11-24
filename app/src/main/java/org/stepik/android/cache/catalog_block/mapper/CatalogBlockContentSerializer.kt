package org.stepik.android.cache.catalog_block.mapper

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.stepik.android.domain.catalog_block.model.AuthorCatalogBlockContentItem
import org.stepik.android.domain.catalog_block.model.CatalogBlockContent
import org.stepik.android.domain.catalog_block.model.StandardCatalogBlockContentItem

class CatalogBlockContentSerializer {
    private val gson = Gson()
    private val jsonParser = JsonParser()

    fun mapToLocalEntity(kind: String, content: CatalogBlockContent?): String {
        val parsedContent = gson.toJsonTree(content)
        (parsedContent as JsonObject).addProperty("kind", kind)
        return parsedContent.toString()
    }

    fun mapToDomainEntity(value: String?): CatalogBlockContent {
        val parsed = jsonParser.parse(value) as JsonObject
        val kind = parsed.remove("kind").asString
        val contentField = parsed["content"]
        return when (kind) {
            CatalogBlockContent.FULL_COURSE_LISTS ->
                CatalogBlockContent.FullCourseList(gson.fromJson(contentField, StandardCatalogBlockContentItem::class.java))

            CatalogBlockContent.SIMPLE_COURSE_LISTS ->
                CatalogBlockContent.SimpleCourseList(gson.fromJson(contentField, TypeToken.getParameterized(ArrayList::class.java, StandardCatalogBlockContentItem::class.java).type))

            CatalogBlockContent.AUTHORS ->
                CatalogBlockContent.AuthorCourseList(gson.fromJson(contentField, TypeToken.getParameterized(ArrayList::class.java, AuthorCatalogBlockContentItem::class.java).type))

            CatalogBlockContent.ORGANIZATIONS ->
                CatalogBlockContent.OrganizationCourseList(gson.fromJson(contentField, TypeToken.getParameterized(ArrayList::class.java, AuthorCatalogBlockContentItem::class.java).type))

            else ->
                CatalogBlockContent.UnsupportedList
        }
    }
}