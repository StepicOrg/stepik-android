package org.stepik.android.model.structure

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Progressable
import org.stepik.android.model.readBoolean
import org.stepik.android.model.writeBoolean

import java.io.Serializable
import java.util.Date

class Unit(
        val id: Long = 0,
        val section: Long = 0,
        val lesson: Long = 0,
        val assignments: LongArray? = null,
        val position: Int = 0,
        override val progress: String? = null,

        @SerializedName("begin_date")
        val beginDate: Date? = null,
        @SerializedName("end_date")
        val endDate: Date? = null,
        @SerializedName("soft_deadline")
        val softDeadline: Date? = null,
        @SerializedName("hard_deadline")
        val hardDeadline: Date? = null,

        @SerializedName("grading_policy")
        val gradingPolicy: String? = null,

        @SerializedName("begin_date_source")
        val beginDateSource: String? = null,
        @SerializedName("end_date_source")
        val endDateSource: String? = null,
        @SerializedName("soft_deadline_source")
        val softDeadlineSource: String? = null,
        @SerializedName("hard_deadline_source")
        val hardDeadlineSource: String? = null,
        @SerializedName("grading_policy_source")
        val gradingPolicySource: String? = null,

        @SerializedName("is_active")
        val isActive: Boolean = false,

        @SerializedName("create_date")
        val createDate: Date? = null,
        @SerializedName("update_date")
        val updateDate: Date? = null,

        @Deprecated("")
        var is_viewed_custom: Boolean = false
) : Serializable, Parcelable, Progressable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.section)
        dest.writeLong(this.lesson)
        dest.writeLongArray(this.assignments)
        dest.writeInt(this.position)
        dest.writeString(this.progress)
        dest.writeSerializable(this.beginDate)
        dest.writeSerializable(this.endDate)
        dest.writeSerializable(this.softDeadline)
        dest.writeSerializable(this.hardDeadline)
        dest.writeString(this.gradingPolicy)
        dest.writeString(this.beginDateSource)
        dest.writeString(this.endDateSource)
        dest.writeString(this.softDeadlineSource)
        dest.writeString(this.hardDeadlineSource)
        dest.writeString(this.gradingPolicySource)
        dest.writeBoolean(isActive)
        dest.writeSerializable(this.createDate)
        dest.writeSerializable(this.updateDate)
        dest.writeBoolean(is_viewed_custom)
    }

    companion object CREATOR : Parcelable.Creator<Unit> {
        override fun createFromParcel(parcel: Parcel): Unit = Unit(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.createLongArray(),
                parcel.readInt(),
                parcel.readString(),
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readBoolean()
        )

        override fun newArray(size: Int): Array<Unit?> = arrayOfNulls(size)
    }
}
