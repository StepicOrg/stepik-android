package org.stepik.android.domain.catalog_block.model

sealed class CatalogBlockContent {
    data class FullCourseList(val courseList: CatalogCourseList) : CatalogBlockContent()
    data class SimpleCourseLists(val courseLists: List<CatalogCourseList>) : CatalogBlockContent()
    data class AuthorsList(val authors: List<CatalogAuthor>) : CatalogBlockContent()
    object Unsupported : CatalogBlockContent()

    companion object {
        /**
         * Types of content
         */
        const val FULL_COURSE_LISTS = "full_course_lists"
        const val SIMPLE_COURSE_LISTS = "simple_course_lists"
        const val AUTHORS = "authors"
        const val UNSUPPORTED = "unsupported"

        /**
         * Appearance of content
         */
        const val APPEARANCE_DEFAULT = "default"
        const val APPEARANCE_SIMPLE_COURSE_LISTS_GRID = "simple_course_lists_grid"
    }
}