package org.stepik.android.domain.course_list.model

import android.os.Parcel
import android.os.Parcelable

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

    companion object CREATOR : Parcelable.Creator<CourseListQuery> {

        override fun createFromParcel(parcel: Parcel): CourseListQuery =
            CourseListQuery(
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Order::class.java.classLoader) as Order?,
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