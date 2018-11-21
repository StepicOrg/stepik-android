package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

class Course(
        val id: Long,
        val title: String? = null,
        val description: String? = null,
        val cover: String? = null,

        val certificate: String? = null,
        val requirements: String? = null,
        val summary: String? = null,
        val workload: String? = null,
        val intro: String? = null,
        val language: String? = null,

        val instructors: LongArray? = null,
        val sections: LongArray? = null,

        @SerializedName("course_format")
        val courseFormat: String? = null,
        @SerializedName("target_audience")
        val targetAudience: String? = null,
        @SerializedName("certificate_footer")
        val certificateFooter: String? = null,
        @SerializedName("certificate_cover_org")
        val certificateCoverOrg: String? = null,

        @SerializedName("total_units")
        val totalUnits: Int = 0,

        var enrollment: Int = 0,
        override val progress: String? = null,
        @SerializedName("owner")
        val owner: Long = 0,

        @SerializedName("is_contest")
        val isContest: Boolean = false,
        @SerializedName("is_featured")
        val isFeatured: Boolean = false,
        @SerializedName("is_spoc")
        val isSpoc: Boolean = false,
        @SerializedName("is_active")
        val isActive: Boolean = false,
        @SerializedName("is_public")
        val isPublic: Boolean = false,

        @SerializedName("certificate_distinction_threshold")
        val certificateDistinctionThreshold: Int = 0,
        @SerializedName("certificate_regular_threshold")
        val certificateRegularThreshold: Int = 0,
        @SerializedName("certificate_link")
        val certificateLink: String? = null,
        @SerializedName("is_certificate_auto_issued")
        val isCertificateAutoIssued: Boolean = false,

        // todo: convert dates to Date
        @SerializedName("last_deadline")
        val lastDeadline: String? = null,
        @SerializedName("begin_date")
        val beginDate: String? = null,
        @SerializedName("end_date")
        val endDate: String? = null,

        val slug: String? = null,

        @SerializedName("intro_video")
        var introVideo: Video? = null,
        @SerializedName("intro_video_id")
        val introVideoId: Long = 0,

        @SerializedName("schedule_link")
        val scheduleLink: String? = null,
        @SerializedName("schedule_long_link")
        val scheduleLongLink: String? = null,

        @SerializedName("last_step")
        val lastStepId: String? = null,
        @SerializedName("learners_count")
        val learnersCount: Long = 0,
        @SerializedName("review_summary")
        val reviewSummary: Long = 0,

        @SerializedName("time_to_complete")
        val timeToComplete: Long? = null,

        var progressObject: Progress? = null,
        var rating: Double = 0.0
): Progressable, Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(cover)
        parcel.writeString(certificate)
        parcel.writeString(requirements)
        parcel.writeString(summary)
        parcel.writeString(workload)
        parcel.writeString(intro)
        parcel.writeString(language)
        parcel.writeLongArray(instructors)
        parcel.writeLongArray(sections)
        parcel.writeString(courseFormat)
        parcel.writeString(targetAudience)
        parcel.writeString(certificateFooter)
        parcel.writeString(certificateCoverOrg)
        parcel.writeInt(totalUnits)
        parcel.writeInt(enrollment)
        parcel.writeString(progress)
        parcel.writeLong(owner)
        parcel.writeBoolean(isContest)
        parcel.writeBoolean(isFeatured)
        parcel.writeBoolean(isSpoc)
        parcel.writeBoolean(isActive)
        parcel.writeBoolean(isPublic)
        parcel.writeInt(certificateDistinctionThreshold)
        parcel.writeInt(certificateRegularThreshold)
        parcel.writeString(certificateLink)
        parcel.writeBoolean(isCertificateAutoIssued)
        parcel.writeString(lastDeadline)
        parcel.writeString(beginDate)
        parcel.writeString(endDate)
        parcel.writeString(slug)
        parcel.writeParcelable(introVideo, flags)
        parcel.writeLong(introVideoId)
        parcel.writeString(scheduleLink)
        parcel.writeString(scheduleLongLink)
        parcel.writeString(lastStepId)
        parcel.writeLong(learnersCount)
        parcel.writeLong(reviewSummary)
        parcel.writeValue(timeToComplete)
        parcel.writeParcelable(progressObject, flags)
        parcel.writeDouble(rating)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Course> {
        override fun createFromParcel(parcel: Parcel): Course = Course(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.createLongArray(),
                parcel.createLongArray(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readParcelable(Video::class.java.classLoader),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readValue(Long::class.java.classLoader) as Long?,
                parcel.readParcelable(Progress::class.java.classLoader),
                parcel.readDouble()
        )

        override fun newArray(size: Int): Array<Course?> = arrayOfNulls(size)
    }
}