package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.app.core.model.Identifiable

@Parcelize
data class CourseCollection(
    @SerializedName("id")
    override val id: Long,
    @SerializedName("position")
    val position: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("language")
    val language: String,
    @SerializedName("courses")
    val courses: List<Long>,
    @SerializedName("description")
    val description: String,
    @SerializedName("platform")
    val platform: Int,
    @SerializedName("similar_authors")
    val similarAuthors: List<Long>,
    @SerializedName("similar_course_lists")
    val similarCourseLists: List<Long>
) : Parcelable, Identifiable<Long>