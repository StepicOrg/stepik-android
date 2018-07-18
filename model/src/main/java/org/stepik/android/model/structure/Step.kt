package org.stepik.android.model.structure

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Progressable
import org.stepik.android.model.readBoolean
import org.stepik.android.model.readParcelable
import org.stepik.android.model.writeBoolean

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

        // todo remove after downloads refactoring
        var isCached: Boolean = false,
        var isLoading: Boolean = false,
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
        dest.writeInt(this.discussionsCount)
        dest.writeString(this.discussionProxy)
        dest.writeInt(this.status?.ordinal ?: -1)
        dest.writeParcelable(this.block, 0)
        dest.writeString(this.progress)
        dest.writeList(this.subscriptions)
        dest.writeLong(this.viewedBy)
        dest.writeLong(this.passedBy)
        dest.writeSerializable(this.createDate)
        dest.writeSerializable(this.updateDate)
        dest.writeBoolean(isCached)
        dest.writeBoolean(isLoading)
        dest.writeBoolean(isCustomPassed)
        dest.writeParcelable(this.actions, flags)
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(parcel: Parcel): Step? = Step(
                id = parcel.readLong(),
                lesson = parcel.readLong(),
                position = parcel.readLong(),
                discussionsCount = parcel.readInt(),
                discussionProxy = parcel.readString(),
                status = Status.values().getOrNull(parcel.readInt()),
                block = parcel.readParcelable(),
                progress = parcel.readString(),
                subscriptions = parcel.createStringArrayList(),
                viewedBy = parcel.readLong(),
                passedBy = parcel.readLong(),
                createDate = parcel.readSerializable() as? Date,
                updateDate = parcel.readSerializable() as? Date,
                isCached = parcel.readBoolean(),
                isLoading = parcel.readBoolean(),
                isCustomPassed = parcel.readBoolean(),
                actions = parcel.readParcelable()
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