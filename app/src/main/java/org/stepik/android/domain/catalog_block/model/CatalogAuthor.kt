package org.stepik.android.domain.catalog_block.model

import com.google.gson.annotations.SerializedName

/**
 * Represents lighter version of [User] and used in catalog blocks
 */
data class CatalogAuthor(
    @SerializedName("id")
    val id: Long,
    @SerializedName("is_organization")
    val isOrganization: Boolean,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("alias")
    val alias: String?,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("created_courses_count")
    val createdCoursesCount: Int,
    @SerializedName("followers_count")
    val followersCount: Int
)