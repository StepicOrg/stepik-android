package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.*

import java.util.Date

data class Step(
        val id: Long = 0,
        val lesson: Long = 0,
        val position: Long = 0,
        val status: Status? = null,
        var block: Block? = null,
        override val progress: String? = null,
        val subscriptions: List<String>? = null,

        @SerializedName("viewed_by")
        val viewedBy: Long = 0,
        @SerializedName("passed_by")
        val passedBy: Long = 0,

        @SerializedName("create_date")
        val createDate: Date? = null,
        @SerializedName("update_date")
        val updateDate: Date? = null,

        var isCustomPassed: Boolean = false,
        val actions: Actions? = null,

        @SerializedName("discussions_count")
        var discussionsCount: Int = 0,
        @SerializedName("discussion_proxy")
        var discussionProxy: String? = null,
        @SerializedName("has_submissions_restrictions")
        val hasSubmissionRestriction: Boolean = false,
        @SerializedName("max_submissions_count")
        val maxSubmissionCount: Int = 0
) : Parcelable, Progressable {
    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.lesson)
        dest.writeLong(this.position)
        dest.writeInt(this.status?.ordinal ?: -1)
        dest.writeParcelable(this.block, 0)
        dest.writeString(this.progress)
        dest.writeStringList(this.subscriptions)

        dest.writeLong(this.viewedBy)
        dest.writeLong(this.passedBy)

        dest.writeDate(this.createDate)
        dest.writeDate(this.updateDate)

        dest.writeBoolean(isCustomPassed)
        dest.writeParcelable(this.actions, flags)

        dest.writeInt(this.discussionsCount)
        dest.writeString(this.discussionProxy)
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(parcel: Parcel): Step = Step(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                Status.values().getOrNull(parcel.readInt()),
                parcel.readParcelable(Block::class.java.classLoader),
                parcel.readString(),
                parcel.createStringArrayList(),

                parcel.readLong(),
                parcel.readLong(),

                parcel.readDate(),
                parcel.readDate(),

                parcel.readBoolean(),
                parcel.readParcelable(Actions::class.java.classLoader),

                parcel.readInt(),
                parcel.readString()
        )

        override fun newArray(size: Int): Array<out Step?> = arrayOfNulls(size)
    }

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