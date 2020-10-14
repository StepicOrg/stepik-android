package org.stepik.android.model.comments

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

import org.stepik.android.model.UserRole
import org.stepik.android.model.Actions
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeBoolean
import org.stepik.android.model.util.writeDate
import java.util.Date

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
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeValue(parent)
        dest.writeValue(user)
        dest.writeInt(userRole?.ordinal ?: -1)
        dest.writeDate(time)
        dest.writeString(text)
        dest.writeValue(replyCount)
        dest.writeValue(submission)
        dest.writeValue(isDeleted)
        dest.writeString(deletedBy)
        dest.writeString(deletedAt)
        dest.writeValue(canModerate)
        dest.writeValue(canDelete)
        dest.writeParcelable(actions, 0)
        dest.writeLong(target)
        dest.writeList(replies)
        dest.writeValue(tonalityAuto)
        dest.writeValue(tonalityManual)
        dest.writeBoolean(isPinned)
        dest.writeValue(isStaffReplied)
        dest.writeValue(isReported)
        dest.writeValue(epicCount)
        dest.writeValue(abuseCount)
        dest.writeString(vote)
    }

    companion object CREATOR: Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment =
            Comment(
                parcel.readLong(),
                parcel.readValue(Long::class.java.classLoader) as Long?,
                parcel.readValue(Long::class.java.classLoader) as Long?,
                UserRole.values().getOrNull(parcel.readInt()),
                parcel.readDate(),
                parcel.readString() ?: "",
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Long::class.java.classLoader) as Long?,
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readParcelable(Actions::class.java.classLoader),
                parcel.readLong(),
                ArrayList<Long>().apply { parcel.readList(this as List<Long>, Long::class.java.classLoader) },
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readBoolean(),
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readValue(Int::class.java.classLoader) as Int?,
                parcel.readString()
            )

        override fun newArray(size: Int): Array<Comment?> =
            arrayOfNulls(size)
    }
}