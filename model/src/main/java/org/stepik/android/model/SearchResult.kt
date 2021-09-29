package org.stepik.android.model

import com.google.gson.annotations.SerializedName

data class SearchResult(
    @SerializedName("id")
    val id: Long,
    @SerializedName("position")
    val position: Int,
    @SerializedName("score")
    val score: String?, // it is not String, but let String
    @SerializedName("course")
    val course: Long?,
    @SerializedName("course_cover")
    val courseCover: String?,
    @SerializedName("course_owner")
    val courseOwner: Long?,
    @SerializedName("course_title")
    val courseTitle: String?,
    @SerializedName("course_slug")
    val courseSlug: String?,
    @SerializedName("lesson")
    val lesson: Long?,
    @SerializedName("lesson_owner")
    val lessonOwner: Long?,
    @SerializedName("lesson_title")
    val lessonTitle: String?,
    @SerializedName("lesson_slug")
    val lessonSlug: String?,
    @SerializedName("lesson_cover_url")
    val lessonCoverUrl: String?,
    @SerializedName("step")
    val step: Long?,
    @SerializedName("step_position")
    val stepPosition: Int?,
    @SerializedName("comment")
    val comment: Long?,
    @SerializedName("comment_parent")
    val commentParent: Long?,
    @SerializedName("comment_user")
    val commentUser: Long?,
    @SerializedName("comment_text")
    val commentText: String?,
    @SerializedName("target_type")
    val targetType: String?
)