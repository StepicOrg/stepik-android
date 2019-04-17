package org.stepik.android.domain.course_reviews.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeDate
import java.util.Date

data class CourseReview(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("course")
    val course: Long = 0,

    @SerializedName("user")
    val user: Long = 0,

    @SerializedName("score")
    val score: Int = 0,

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("create_date")
    val createDate: Date? = null,

    @SerializedName("update_date")
    val updateDate: Date? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(course)
        parcel.writeLong(user)
        parcel.writeInt(score)
        parcel.writeString(text)
        parcel.writeDate(createDate)
        parcel.writeDate(updateDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseReview> {
        override fun createFromParcel(parcel: Parcel): CourseReview =
            CourseReview(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readDate(),
                parcel.readDate()
            )

        override fun newArray(size: Int): Array<CourseReview?> =
            arrayOfNulls(size)
    }
}