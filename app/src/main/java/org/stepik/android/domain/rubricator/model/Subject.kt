package org.stepik.android.domain.rubricator.model

import com.google.gson.annotations.SerializedName

data class Subject(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("title_en")
    val titleEnglish: String,
    @SerializedName("meta_categories")
    val metaCategories: List<Long>
)