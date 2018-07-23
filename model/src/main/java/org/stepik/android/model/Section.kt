package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

import java.util.Date

data class Section(
        val id: Long = 0,
        val course: Long = 0, // course id
        val units: List<Long> = emptyList(),
        val position: Int = 0,
        override val progress: String? = null,
        val title: String? = null,
        val slug: String? = null,

        @SerializedName("begin_date")
        val beginDate: Date? = null,
        @SerializedName("end_date")
        val endDate: Date? = null,
        @SerializedName("soft_deadline")
        val softDeadline: Date? = null,
        @SerializedName("hard_deadline")
        val hardDeadline: Date? = null,

        @SerializedName("create_date")
        val createDate: Date? = null,
        @SerializedName("update_date")
        val updateDate: Date? = null,

        @SerializedName("grading_policy")
        val gradingPolicy: String? = null,
        @SerializedName("is_active")
        val isActive: Boolean = false,

        val actions: Actions? = null,
        @SerializedName("is_exam")
        val isExam: Boolean = false,
        @SerializedName("discounting_policy")
        val discountingPolicy: DiscountingPolicyType? = null,
        @SerializedName("is_requirement_satisfied")
        val isRequirementSatisfied: Boolean = true,
        @SerializedName("required_section")
        val requiredSection: Long = 0, //id of required section
        @SerializedName("required_percent")
        val requiredPercent: Int = 0,

        // todo: remove this after downloads refactoring
        var isCached: Boolean = false,
        var isLoading: Boolean = false
) : Parcelable, Progressable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(course)
        parcel.writeList(units)
        parcel.writeInt(position)
        parcel.writeString(progress)
        parcel.writeString(title)
        parcel.writeString(slug)
        parcel.writeSerializable(beginDate)
        parcel.writeSerializable(endDate)
        parcel.writeSerializable(softDeadline)
        parcel.writeSerializable(hardDeadline)
        parcel.writeSerializable(createDate)
        parcel.writeSerializable(updateDate)
        parcel.writeString(gradingPolicy)
        parcel.writeBoolean(isActive)
        parcel.writeParcelable(actions, flags)
        parcel.writeBoolean(isExam)
        parcel.writeInt(discountingPolicy?.ordinal ?: -1)
        parcel.writeBoolean(isRequirementSatisfied)
        parcel.writeLong(requiredSection)
        parcel.writeInt(requiredPercent)
        parcel.writeBoolean(isCached)
        parcel.writeBoolean(isLoading)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Section> {
        override fun createFromParcel(parcel: Parcel) = Section(
                parcel.readLong(),
                parcel.readLong(),
                mutableListOf<Long>().apply { parcel.readList(this, Long::class.java.classLoader) },
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readSerializable() as? Date,
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readParcelable(Actions::class.java.classLoader),
                parcel.readBoolean(),
                DiscountingPolicyType.values().getOrNull(parcel.readInt()),
                parcel.readBoolean(),
                parcel.readLong(),
                parcel.readInt(),
                parcel.readBoolean(),
                parcel.readBoolean()
        )

        override fun newArray(size: Int): Array<Section?> = arrayOfNulls(size)
    }
}