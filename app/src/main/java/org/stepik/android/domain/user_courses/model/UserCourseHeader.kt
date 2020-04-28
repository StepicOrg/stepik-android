package org.stepik.android.domain.user_courses.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class UserCourseHeader : Parcelable {
    @Parcelize
    object Empty : UserCourseHeader()
    @Parcelize
    class Data(val userCourse: UserCourse) : UserCourseHeader()
}