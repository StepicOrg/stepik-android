package org.stepik.android.domain.catalog_block.model

import com.google.gson.annotations.SerializedName

data class CatalogBlockItem(
    @SerializedName("id")
    val id: Long,
    @SerializedName("position")
    val position: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("kind")
    val kind: Kind?,
    @SerializedName("appearance")
    val appearance: String,
    @SerializedName("is_title_visible")
    val isTitleVisible: Boolean,
//    @SerializedName("content")
    val content: List<CatalogBlockContentItem>
) {
    enum class Kind {
        @SerializedName("full_course_lists")
        FULL_COURSE_LISTS,
        @SerializedName("simple_course_lists")
        SIMPLE_COURSE_LISTS,
        @SerializedName("organizations")
        ORGANIZATIONS,
        @SerializedName("authors")
        AUTHORS,
        @SerializedName("specializations")
        SPECIALIZATIONS
    }
}