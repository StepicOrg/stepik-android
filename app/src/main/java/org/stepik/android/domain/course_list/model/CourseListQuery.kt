package org.stepik.android.domain.course_list.model

import android.os.Parcel
import android.os.Parcelable

data class CourseListQuery(
    val page: Int? = null,
    val order: String? = null,
    val teacher: Long? = null,

    val isExcludeEnded: Boolean? = null,
    val isPublic: Boolean? = null
) : Parcelable {
    companion object CREATOR : Parcelable.Creator<CourseListQuery> {
        const val ORDER_ACTIVITY_DESC = "-activity"
        const val ORDER_POPULARITY_DESC = "-popularity"

        override fun createFromParcel(parcel: Parcel): CourseListQuery =
            CourseListQuery(
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(String::class.java.classLoader) as String?,
                parcel.readValue(Long::class.java.classLoader) as Long?,
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?
            )

        override fun newArray(size: Int): Array<CourseListQuery?> =
            arrayOfNulls(size)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(page)
        parcel.writeValue(order)
        parcel.writeValue(teacher)
        parcel.writeValue(isExcludeEnded)
        parcel.writeValue(isPublic)
    }

    override fun describeContents(): Int = 0
}