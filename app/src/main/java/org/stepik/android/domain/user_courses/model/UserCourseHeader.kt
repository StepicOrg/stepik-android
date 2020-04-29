package org.stepik.android.domain.user_courses.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class UserCourseHeader : Parcelable {
    @Parcelize
    object Empty : UserCourseHeader()
    @Parcelize
    data class Data(val userCourse: UserCourse, val isSending: Boolean) : UserCourseHeader()
}