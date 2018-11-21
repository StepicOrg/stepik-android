package org.stepik.android.domain.course_info.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Video
import org.stepik.android.model.user.User

/**
 * Data class to represent course info state
 * null field means no block will be displayed
 * null items in instructor block means that users not loaded yet
 */
data class CourseInfoData(
    val organization: String? = null,
    val video: Video? = null,
    val about: String? = null,
    val requirements: String? = null,
    val targetAudience: String? = null,
    val timeToComplete: Long = 0,
    val instructors: List<User?>? = null,
    val language: String? = null,
    val certificate: Certificate? = null
) : Parcelable {
    data class Certificate(
        val title: String,
        val distinctionThreshold: Int,
        val regularThreshold: Int
    ) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeInt(distinctionThreshold)
            parcel.writeInt(regularThreshold)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Certificate> {
            override fun createFromParcel(parcel: Parcel) =
                Certificate(
                    parcel.readString()!!,
                    parcel.readInt(),
                    parcel.readInt()
                )

            override fun newArray(size: Int): Array<Certificate?> =
                arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(organization)
        parcel.writeParcelable(video, flags)
        parcel.writeString(about)
        parcel.writeString(requirements)
        parcel.writeString(targetAudience)
        parcel.writeLong(timeToComplete)
        parcel.writeTypedList(instructors)
        parcel.writeString(language)
        parcel.writeParcelable(certificate, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseInfoData> {
        override fun createFromParcel(parcel: Parcel) =
            CourseInfoData(
                parcel.readString(),
                parcel.readParcelable(Video::class.java.classLoader),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.createTypedArrayList(User),
                parcel.readString(),
                parcel.readParcelable(Certificate::class.java.classLoader)
            )

        override fun newArray(size: Int): Array<CourseInfoData?> =
            arrayOfNulls(size)
    }
}