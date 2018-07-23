package org.stepik.android.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
        val id: String?,
        val score: String?, // it is not String, but let String
        val course: Long = 0,
        @SerializedName("course_cover")
        val courseCover: String?,
        @SerializedName("course_owner")
        val courseOwner: String?,// it is number
        @SerializedName("course_title")
        val courseTitle: String?,
        @SerializedName("course_slug")
        val courseSlug: String?
)
