package org.stepik.android.domain.visited_courses.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(indices = [Index(value = ["course"], unique = true)])
data class VisitedCourse(
    @SerializedName("course")
    val course: Long
) {
    @PrimaryKey
    @SerializedName("id")
    var id: Long = 0
}