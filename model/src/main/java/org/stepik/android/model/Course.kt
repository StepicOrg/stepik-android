package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class Course(
    @SerializedName("id")
    override val id: Long,
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
    val authors: List<Long>? = null,
    @SerializedName("instructors")
    val instructors: List<Long>? = null,
    @SerializedName("sections")
    val sections: List<Long>? = null,

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
    @SerializedName("is_archived")
    val isArchived: Boolean = false,
    @SerializedName("is_favorite")
    val isFavorite: Boolean = false,

    @SerializedName("certificate_distinction_threshold")
    val certificateDistinctionThreshold: Long = 0,
    @SerializedName("certificate_regular_threshold")
    val certificateRegularThreshold: Long = 0,
    @SerializedName("certificate_link")
    val certificateLink: String? = null,
    @SerializedName("is_certificate_auto_issued")
    val isCertificateAutoIssued: Boolean = false,
    @SerializedName("is_certificate_issued")
    val isCertificateIssued: Boolean = false,

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

    @SerializedName("options")
    val courseOptions: CourseOptions? = null,

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
    val priceTier: String? = null
) : Progressable, Parcelable, Identifiable<Long> {

    val hasCertificate: Boolean
        get() = certificate?.let {
            val hasText = it.isNotEmpty()
            val anyCertificateThreshold = certificateRegularThreshold > 0 || certificateDistinctionThreshold > 0
            anyCertificateThreshold && (hasText || (isCertificateAutoIssued && isCertificateIssued))
        } ?: false

    companion object {
        const val SCHEDULE_TYPE_ENDED = "ended"
        const val SCHEDULE_TYPE_UPCOMMING = "upcoming"
    }
}