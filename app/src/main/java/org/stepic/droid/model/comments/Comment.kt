package org.stepic.droid.model.comments

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

import org.stepic.droid.model.UserRole
import org.stepik.android.model.structure.Actions


data class Comment(
        var id: Long = 0,
        var parent: Long? = null,
        val user: Long? = null,
        @SerializedName("user_role")
        val userRole: UserRole? = null,
        val time: String? = null,
        var text: String? = "",
        @SerializedName("reply_count")
        val replyCount: Int? = null,
        var is_deleted: Boolean? = null,
        val deleted_by: String? = null,
        val deleted_at: String? = null,
        val can_moderate: Boolean? = null,
        val can_delete: Boolean? = null,
        val actions: Actions? = null,
        var target: Long = 0, //for example, id of Step.
        val replies: List<Long>? = null, //oldList of all replies, but in query only 20.
        val tonality_auto: Int? = null,
        val tonality_manual: Int? = null,
        @SerializedName("is_pinned")
        val isPinned: Boolean = false,
        val is_staff_replied: Boolean? = null,
        val is_reported: Boolean? = null,
        val epic_count: Int? = null,
        val abuse_count: Int? = null,
        val vote: String? = null
) : Parcelable {
    constructor(target: Long, text: String, parent: Long?) : this() {
        this.target = target
        this.text = text
        this.parent = parent
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Comment> = object : Parcelable.Creator<Comment> {
            override fun createFromParcel(source: Parcel): Comment = Comment(source)
            override fun newArray(size: Int): Array<Comment?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readValue(Int::class.java.classLoader)?.let { UserRole.values()[it as Int] },
            source.readString(),
            source.readString()?:"",
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readString(),
            source.readString(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readParcelable<Actions>(Actions::class.java.classLoader),
            source.readLong(),
            ArrayList<Long>().apply { source.readList(this, Long::class.java.classLoader) },
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            1 == source.readInt(),
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Boolean::class.java.classLoader) as Boolean?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeValue(parent)
        dest.writeValue(user)
        dest.writeValue(userRole?.ordinal)
        dest.writeString(time)
        dest.writeString(text)
        dest.writeValue(replyCount)
        dest.writeValue(is_deleted)
        dest.writeString(deleted_by)
        dest.writeString(deleted_at)
        dest.writeValue(can_moderate)
        dest.writeValue(can_delete)
        dest.writeParcelable(actions, 0)
        dest.writeLong(target)
        dest.writeList(replies)
        dest.writeValue(tonality_auto)
        dest.writeValue(tonality_manual)
        dest.writeInt((if (isPinned) 1 else 0))
        dest.writeValue(is_staff_replied)
        dest.writeValue(is_reported)
        dest.writeValue(epic_count)
        dest.writeValue(abuse_count)
        dest.writeString(vote)
    }

}