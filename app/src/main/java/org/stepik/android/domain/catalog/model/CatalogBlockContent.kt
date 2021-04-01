package org.stepik.android.domain.catalog.model

import com.google.gson.annotations.SerializedName

sealed class CatalogBlockContent {
    data class FullCourseList(
        @SerializedName("courseList")
        val courseList: CatalogCourseList
    ) : CatalogBlockContent()
    data class SimpleCourseLists(
        @SerializedName("courseLists")
        val courseLists: List<CatalogCourseList>
    ) : CatalogBlockContent()
    data class AuthorsList(
        @SerializedName("authors")
        val authors: List<CatalogAuthor>
    ) : CatalogBlockContent()
    object RecommendedCourses : CatalogBlockContent()
    object Unsupported : CatalogBlockContent()

    companion object {
        /**
         * Types of content
         */
        const val FULL_COURSE_LISTS = "full_course_lists"
        const val SIMPLE_COURSE_LISTS = "simple_course_lists"
        const val AUTHORS = "authors"
        const val RECOMMENDED_COURSES = "recommended_courses"
        const val UNSUPPORTED = "unsupported"

        /**
         * Appearance of content
         */
        const val APPEARANCE_DEFAULT = "default"
        const val APPEARANCE_SIMPLE_COURSE_LISTS_GRID = "simple_course_lists_grid"
    }
}