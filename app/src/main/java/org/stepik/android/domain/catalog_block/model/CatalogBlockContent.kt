package org.stepik.android.domain.catalog_block.model

sealed class CatalogBlockContent {
    data class FullCourseList(val content: StandardCatalogBlockContentItem) : CatalogBlockContent()
    data class SimpleCourseList(val content: List<StandardCatalogBlockContentItem>) : CatalogBlockContent()
    data class AuthorCourseList(val content: List<AuthorCatalogBlockContentItem>) : CatalogBlockContent()
    data class OrganizationCourseList(val content: List<AuthorCatalogBlockContentItem>) : CatalogBlockContent()
    object UnsupportedList : CatalogBlockContent()

    companion object {
        const val FULL_COURSE_LISTS = "full_course_lists"
        const val SIMPLE_COURSE_LISTS = "simple_course_lists"
        const val AUTHORS = "authors"
        const val ORGANIZATIONS = "organizations"
        const val UNSUPPORTED = "unsupported"
    }
}