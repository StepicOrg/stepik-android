package org.stepik.android.domain.course_info.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData

/**
 * Data class to represent course info state
 * null field means no block will be displayed
 * null items in instructor block means that users not loaded yet
 */
data class CourseInfoData(
    val organization: User? = null,
    val videoMediaData: VideoPlayerMediaData? = null,
    val about: String? = null,
    val requirements: String? = null,
    val targetAudience: String? = null,
    val timeToComplete: Long = 0,
    val instructors: List<User?>? = null,
    val language: String? = null,
    val certificate: Certificate? = null,
    val learnersCount: Long = 0
) : Parcelable {
    data class Certificate(
        val title: String,
        val distinctionThreshold: Long,
        val regularThreshold: Long
    ) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(title)
            parcel.writeLong(distinctionThreshold)
            parcel.writeLong(regularThreshold)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Certificate> {
            override fun createFromParcel(parcel: Parcel): Certificate =
                Certificate(
                    parcel.readString()!!,
                    parcel.readLong(),
                    parcel.readLong()
                )

            override fun newArray(size: Int): Array<Certificate?> =
                arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(organization, flags)
        parcel.writeParcelable(videoMediaData, flags)
        parcel.writeString(about)
        parcel.writeString(requirements)
        parcel.writeString(targetAudience)
        parcel.writeLong(timeToComplete)
        parcel.writeTypedList(instructors)
        parcel.writeString(language)
        parcel.writeParcelable(certificate, flags)
        parcel.writeLong(learnersCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseInfoData> {
        override fun createFromParcel(parcel: Parcel): CourseInfoData =
            CourseInfoData(
                parcel.readParcelable(User::class.java.classLoader),
                parcel.readParcelable(VideoPlayerMediaData::class.java.classLoader),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.createTypedArrayList(User),
                parcel.readString(),
                parcel.readParcelable(Certificate::class.java.classLoader),
                parcel.readLong()
            )

        override fun newArray(size: Int): Array<CourseInfoData?> =
            arrayOfNulls(size)
    }
}