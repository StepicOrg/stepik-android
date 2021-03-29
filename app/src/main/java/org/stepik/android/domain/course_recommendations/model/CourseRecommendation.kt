package org.stepik.android.domain.course_recommendations.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class CourseRecommendation(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("courses")
    val courses: List<Long>
)
