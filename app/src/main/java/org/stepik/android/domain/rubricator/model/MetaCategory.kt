package org.stepik.android.domain.rubricator.model

import com.google.gson.annotations.SerializedName

data class MetaCategory(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("title_en")
    val titleEnglish: String,
    @SerializedName("course_lists")
    val courseLists: List<Long>
)
