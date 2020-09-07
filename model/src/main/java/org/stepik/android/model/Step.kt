package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Step(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("lesson")
    val lesson: Long = 0,
    @SerializedName("position")
    val position: Long = 0,
    @SerializedName("status")
    val status: Status? = null,
    @SerializedName("block")
    var block: Block? = null,
    @SerializedName("progress")
    override val progress: String? = null,
    @SerializedName("subscriptions")
    val subscriptions: List<String>? = null,

    @SerializedName("session")
    val session: Long? = null,
    @SerializedName("instruction")
    val instruction: Long? = null,
    @SerializedName("instruction_type")
    val instructionType: String? = null, //todo enum

    @SerializedName("viewed_by")
    val viewedBy: Long = 0,
    @SerializedName("passed_by")
    val passedBy: Long = 0,

    @SerializedName("worth")
    val worth: Long = 0,

    @SerializedName("create_date")
    val createDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    @SerializedName("actions")
    val actions: Actions? = null,

    @SerializedName("discussions_count")
    var discussionsCount: Int = 0,
    @SerializedName("discussion_proxy")
    var discussionProxy: String? = null,

    @SerializedName("discussion_threads")
    val discussionThreads: List<String>? = null,

    @SerializedName("has_submissions_restrictions")
    val hasSubmissionRestriction: Boolean = false,
    @SerializedName("max_submissions_count")
    val maxSubmissionCount: Int = 0,

    @SerializedName("correct_ratio")
    val correctRatio: Double? = null
) : Parcelable, Progressable {
    enum class Status {
        @SerializedName("ready")
        READY,
        @SerializedName("preparing")
        PREPARING,
        @SerializedName("error")
        ERROR;

        companion object {
            fun byName(serverName: String?): Status? = serverName?.let { name ->
                values().find { value ->
                    value.name.equals(name, ignoreCase = true)
                }
            }
        }
    }
}