package org.stepik.android.domain.catalog_block.model

import com.google.gson.annotations.SerializedName

class SimpleCourseListCatalogBlockContentItem(
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