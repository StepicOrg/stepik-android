package org.stepik.android.domain.course_list.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

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
                parcel.readInt(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readBoolean(),
                parcel.readBoolean()
            )

        override fun newArray(size: Int): Array<CourseListQuery?> =
            arrayOfNulls(size)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(page ?: -1)
        parcel.writeString(order ?: "")
        parcel.writeLong(teacher ?: -1)
        parcel.writeBoolean(isExcludeEnded ?: false)
        parcel.writeBoolean(isPublic ?: false)
    }

    override fun describeContents(): Int = 0
}