package org.stepik.android.domain.course_list.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CourseListQuery(
    val page: Int? = null,
    val order: Order? = null,
    val teacher: Long? = null,

    val isExcludeEnded: Boolean? = null,
    val isPublic: Boolean? = null
) : Parcelable {
    enum class Order(val order: String) {
        ACTIVITY_DESC("-activity"),
        POPULARITY_DESC("-popularity")
    }
}