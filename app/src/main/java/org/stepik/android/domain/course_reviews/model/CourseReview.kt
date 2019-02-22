package org.stepik.android.domain.course_reviews.model

import com.google.gson.annotations.SerializedName
import java.util.Date

class CourseReview(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("course")
    val course: Long = 0,

    @SerializedName("user")
    val user: Long = 0,

    @SerializedName("score")
    val score: Int = 0,

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("create_date")
    val createDate: Date? = null,

    @SerializedName("update_date")
    val updateDate: Date? = null
)