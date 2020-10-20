package org.stepik.android.model.comments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.Actions
import org.stepik.android.model.UserRole
import java.util.*

@Parcelize
data class Comment(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("parent")
    val parent: Long? = null,
    @SerializedName("user")
    val user: Long? = null,
    @SerializedName("user_role")
    val userRole: UserRole? = null,
    @SerializedName("time")
    val time: Date? = null,
    @SerializedName("text")
    val text: String? = "",
    @SerializedName("reply_count")
    val replyCount: Int? = null,
    @SerializedName("submission")
    val submission: Long? = null,

    @SerializedName("is_deleted")
    val isDeleted: Boolean? = null,
    @SerializedName("deleted_by")
    val deletedBy: String? = null,
    @SerializedName("deleted_at")
    val deletedAt: String? = null,

    @SerializedName("can_moderate")
    val canModerate: Boolean? = null,
    @SerializedName("can_delete")
    val canDelete: Boolean? = null,

    @SerializedName("actions")
    val actions: Actions? = null,
    @SerializedName("target")
    val target: Long = 0, //for example, id of Step.
    @SerializedName("replies")
    val replies: List<Long>? = null, //oldList of all replies, but in query only 20.

    @SerializedName("tonality_auto")
    val tonalityAuto: Int? = null,
    @SerializedName("tonality_manual")
    val tonalityManual: Int? = null,
    @SerializedName("is_pinned")
    val isPinned: Boolean = false,
    @SerializedName("is_staff_replied")
    val isStaffReplied: Boolean? = null,
    @SerializedName("is_reported")
    val isReported: Boolean? = null,
    @SerializedName("epic_count")
    val epicCount: Int? = null,
    @SerializedName("abuse_count")
    val abuseCount: Int? = null,

    @SerializedName("vote")
    val vote: String? = null,

    @SerializedName("thread")
    val thread: String? = null
) : Parcelable