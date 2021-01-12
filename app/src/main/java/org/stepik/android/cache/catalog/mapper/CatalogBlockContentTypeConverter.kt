package org.stepik.android.cache.catalog.mapper

import androidx.room.TypeConverter
import org.stepik.android.domain.catalog.model.CatalogBlockContent

class CatalogBlockContentTypeConverter {
    private val catalogBlockContentSerializer = CatalogBlockContentSerializer()

    @TypeConverter
    fun fromCatalogBlockContent(value: CatalogBlockContent?): String? {
        val kind = contentToKind(value)
        return catalogBlockContentSerializer.mapToLocalEntity(kind, value)
    }

    @TypeConverter
    fun toCatalogBlockContent(value: String?): CatalogBlockContent =
        catalogBlockContentSerializer.mapToDomainEntity(value)

    private fun contentToKind(value: CatalogBlockContent?): String =
        when (value) {
            is CatalogBlockContent.FullCourseList ->
                CatalogBlockContent.FULL_COURSE_LISTS

            is CatalogBlockContent.SimpleCourseLists ->
                CatalogBlockContent.SIMPLE_COURSE_LISTS

            is CatalogBlockContent.AuthorsList ->
                CatalogBlockContent.AUTHORS

            else ->
                CatalogBlockContent.UNSUPPORTED
        }
}