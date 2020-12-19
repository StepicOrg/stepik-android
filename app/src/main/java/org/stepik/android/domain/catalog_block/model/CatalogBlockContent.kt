package org.stepik.android.domain.catalog_block.model

sealed class CatalogBlockContent {
    data class FullCourseList(val courseList: CatalogCourseList) : CatalogBlockContent()
    data class SimpleCourseLists(val courseLists: List<CatalogCourseList>) : CatalogBlockContent()
    data class AuthorsList(val authors: List<CatalogAuthor>) : CatalogBlockContent()
    object Unsupported : CatalogBlockContent()

    companion object {
        const val FULL_COURSE_LISTS = "full_course_lists"
        const val SIMPLE_COURSE_LISTS = "simple_course_lists"
        const val AUTHORS = "authors"
        const val UNSUPPORTED = "unsupported"
    }
}