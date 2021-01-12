package org.stepik.android.domain.catalog.model

import com.google.gson.annotations.SerializedName

/**
 * Represents lighter version of [CourseCollection] and used in catalog blocks
 */
data class CatalogCourseList(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("courses")
    val courses: List<Long>,
    @SerializedName("courses_count")
    val coursesCount: Int
)