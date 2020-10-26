package org.stepik.android.domain.visited_courses.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class VisitedCourse(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("course")
    val course: Long
)