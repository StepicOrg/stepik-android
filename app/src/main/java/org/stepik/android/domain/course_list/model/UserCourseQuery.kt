package org.stepik.android.domain.course_list.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCourseQuery(
    val page: Int? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean? = null
) : Parcelable