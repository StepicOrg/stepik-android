package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

class Course(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("cover")
    val cover: String? = null,

    @SerializedName("certificate")
    val certificate: String? = null,
    @SerializedName("requirements")
    val requirements: String? = null,
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("workload")
    val workload: String? = null,
    @SerializedName("intro")
    val intro: String? = null,
    @SerializedName("intro_video")
    var introVideo: Video? = null,
    @SerializedName("language")
    val language: String? = null,

    @SerializedName("authors")
    val authors: LongArray? = null,
    @SerializedName("instructors")
    val instructors: LongArray? = null,
    @SerializedName("sections")
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
    val totalUnits: Long = 0,

    @SerializedName("enrollment")
    var enrollment: Long = 0,
    @SerializedName("progress")
    override val progress: String? = null,
    @SerializedName("owner")
    val owner: Long = 0,

    @SerializedName("readiness")
    val readiness: Double = 0.0,

    @SerializedName("is_contest")
    val isContest: Boolean = false,
    @SerializedName("is_featured")
    val isFeatured: Boolean = false,
    @SerializedName("is_active")
    val isActive: Boolean = false,
    @SerializedName("is_public")
    val isPublic: Boolean = false,

    @SerializedName("certificate_distinction_threshold")
    val certificateDistinctionThreshold: Long = 0,
    @SerializedName("certificate_regular_threshold")
    val certificateRegularThreshold: Long = 0,
    @SerializedName("certificate_link")
    val certificateLink: String? = null,
    @SerializedName("is_certificate_auto_issued")
    val isCertificateAutoIssued: Boolean = false,

    @SerializedName("last_deadline")
    val lastDeadline: String? = null,
    @SerializedName("begin_date")
    val beginDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,

    @SerializedName("slug")
    val slug: String? = null,

    @SerializedName("schedule_link")
    val scheduleLink: String? = null,
    @SerializedName("schedule_long_link")
    val scheduleLongLink: String? = null,
    @SerializedName("schedule_type")
    val scheduleType: String? = null,

    @SerializedName("last_step")
    val lastStepId: String? = null,
    @SerializedName("learners_count")
    val learnersCount: Long = 0,
    @SerializedName("review_summary")
    val reviewSummary: Long = 0,

    @SerializedName("time_to_complete")
    val timeToComplete: Long? = null,

    /**
     * Paid courses fields
     */
    @SerializedName("is_paid")
    val isPaid: Boolean = false,
    @SerializedName("price")
    val price: String? = null,
    @SerializedName("currency_code")
    val currencyCode: String? = null,
    @SerializedName("display_price")
    val displayPrice: String? = null,
    @SerializedName("price_tier")
    val priceTier: String? = null,

    var progressObject: Progress? = null,
    var rating: Double = 0.0
): Progressable, Parcelable {
    override fun equals(other: Any?): Boolean { // todo use data class
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Course

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

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
        parcel.writeParcelable(introVideo, flags)
        parcel.writeString(language)

        parcel.writeLongArray(authors)
        parcel.writeLongArray(instructors)
        parcel.writeLongArray(sections)

        parcel.writeString(courseFormat)
        parcel.writeString(targetAudience)
        parcel.writeString(certificateFooter)
        parcel.writeString(certificateCoverOrg)

        parcel.writeLong(totalUnits)

        parcel.writeLong(enrollment)
        parcel.writeString(progress)
        parcel.writeLong(owner)

        parcel.writeDouble(readiness)

        parcel.writeBoolean(isContest)
        parcel.writeBoolean(isFeatured)
        parcel.writeBoolean(isActive)
        parcel.writeBoolean(isPublic)

        parcel.writeLong(certificateDistinctionThreshold)
        parcel.writeLong(certificateRegularThreshold)
        parcel.writeString(certificateLink)
        parcel.writeBoolean(isCertificateAutoIssued)

        parcel.writeString(lastDeadline)
        parcel.writeString(beginDate)
        parcel.writeString(endDate)

        parcel.writeString(slug)

        parcel.writeString(scheduleLink)
        parcel.writeString(scheduleLongLink)
        parcel.writeString(scheduleType)

        parcel.writeString(lastStepId)
        parcel.writeLong(learnersCount)
        parcel.writeLong(reviewSummary)

        parcel.writeValue(timeToComplete)

        parcel.writeBoolean(isPaid)
        parcel.writeString(price)
        parcel.writeString(currencyCode)
        parcel.writeString(displayPrice)
        parcel.writeString(priceTier)

        parcel.writeParcelable(progressObject, flags)
        parcel.writeDouble(rating)
    }

    override fun describeContents(): Int = 0


    companion object CREATOR : Parcelable.Creator<Course> {
        const val SCHEDULE_TYPE_ENDED = "ended"
        const val SCHEDULE_TYPE_UPCOMMING = "upcoming"

        override fun createFromParcel(parcel: Parcel): Course =
            Course(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readParcelable(Video::class.java.classLoader),
                parcel.readString(),
                parcel.createLongArray(),
                parcel.createLongArray(),
                parcel.createLongArray(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readDouble(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readValue(Long::class.java.classLoader) as Long?,

                parcel.readBoolean(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),

                parcel.readParcelable(Progress::class.java.classLoader),
                parcel.readDouble()
            )

        override fun newArray(size: Int): Array<Course?> = arrayOfNulls(size)
    }
}