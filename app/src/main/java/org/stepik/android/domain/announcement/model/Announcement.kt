package org.stepik.android.domain.announcement.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.nobird.app.core.model.Identifiable
import java.util.Date

@Entity
data class Announcement(
    @SerializedName("id")
    @PrimaryKey
    override val id: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("user")
    val user: Long?,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("create_date")
    val createDate: Date?,
    @SerializedName("next_date")
    val nextDate: Date?,
    @SerializedName("sent_date")
    val sentDate: Date?,
    @SerializedName("status")
    val status: AnnouncementStatus,
    @SerializedName("is_restricted_by_score")
    val isRestrictedByScore: Boolean,
    @SerializedName("score_percent_min")
    val scorePercentMin: Int,
    @SerializedName("score_percent_max")
    val scorePercentMax: Int,
    @SerializedName("email_template")
    val emailTemplate: String?,
    @SerializedName("is_scheduled")
    val isScheduled: Boolean,
    @SerializedName("start_date")
    val startDate: Date?,
    @SerializedName("mail_period_days")
    val mailPeriodDays: Int,
    @SerializedName("mail_quantity")
    val mailQuantity: Int,
    @SerializedName("is_infinite")
    val isInfinite: Boolean,
    @SerializedName("on_enroll")
    val onEnroll: Boolean,
    @SerializedName("publish_count")
    val publishCount: Int?,
    @SerializedName("queue_count")
    val queueCount: Int?,
    @SerializedName("sent_count")
    val sentCount: Int?,
    @SerializedName("open_count")
    val openCount: Int?,
    @SerializedName("click_count")
    val clickCount: Int?,
    @SerializedName("estimated_start_date")
    val estimatedStartDate: Date?,
    @SerializedName("estimated_finish_date")
    val estimatedFinishDate: Date?,
    @SerializedName("notice_dates")
    val noticeDates: List<Date> = emptyList()
) : Identifiable<Long> {
    enum class AnnouncementStatus(val status: String) {
        @SerializedName("composing")
        COMPOSING("composing"),

        @SerializedName("scheduled")
        SCHEDULED("scheduled"),

        @SerializedName("queueing")
        QUEUEING("queueing"),

        @SerializedName("queued")
        QUEUED("queued"),

        @SerializedName("sending")
        SENDING("sending"),

        @SerializedName("sent")
        SENT("sent"),

        @SerializedName("aborted")
        ABORTED("aborted")
    }
}
