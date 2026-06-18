package org.stepik.android.domain.course_list.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserCourseQuery(
    val page: Int? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean? = null,
    val course: Long? = null
) : Parcelable