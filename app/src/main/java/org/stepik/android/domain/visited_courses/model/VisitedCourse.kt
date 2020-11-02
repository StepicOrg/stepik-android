package org.stepik.android.domain.visited_courses.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class VisitedCourse(
    @SerializedName("id")
    val id: Long,
    @PrimaryKey
    @SerializedName("course")
    val course: Long
)