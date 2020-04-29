package org.stepik.android.domain.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Course
import org.stepik.android.domain.user_courses.model.UserCourseHeader

@Parcelize
data class CourseHeaderData(
    val courseId: Long,
    val course: Course,
    val userCourseHeader: UserCourseHeader,
    val title: String,
    val cover: String,

    val stats: CourseStats,
    val localSubmissionsCount: Int
) : Parcelable
