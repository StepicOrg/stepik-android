package org.stepik.android.data.course_list.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseListQueryData(
    val courseListQueryId: String,
    val courses: List<Long>
) : Parcelable