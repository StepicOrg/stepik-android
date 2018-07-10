package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepic.droid.util.DateTimeHelper
import java.io.Serializable
import java.util.*

data class Section(
        var id: Long = 0,
        var course: Long = 0, // course id
        var units: LongArray = longArrayOf(),
        var position: Int = 0,
        var progress: String? = null,
        var title: String? = null,
        var slug: String? = null,
        @SerializedName("begin_date")
        var beginDate: String? = null,
        @SerializedName("end_date")
        var endDate: String? = null,
        @SerializedName("soft_deadline")
        var softDeadline: String? = null,
        @SerializedName("hard_deadline")
        var hardDeadline: String? = null,
        @SerializedName("grading_policy")
        var gradingPolicy: String? = null,
        @SerializedName("is_active")
        var isActive: Boolean = false,
        @SerializedName("create_date")
        var createDate: String? = null,
        @SerializedName("update_date")
        var updateDate: String? = null,
        var actions: org.stepik.android.model.actions.Actions? = null,
        @SerializedName("is_exam")
        var isExam: Boolean = false,
        @SerializedName("discounting_policy")
        var discountingPolicy: DiscountingPolicyType? = null,
        @SerializedName("is_requirement_satisfied")
        var isRequirementSatisfied: Boolean = true,
        @SerializedName("required_section")
        var requiredSection: Long = 0, //id of required section
        @SerializedName("required_percent")
        var requiredPercent: Int = 0) : Serializable, Parcelable {


    var isCached: Boolean = false
    var isLoading: Boolean = false

    val formattedBeginDate: String by lazy {
        DateTimeHelper.getPrintableOfIsoDate(beginDate, datePattern, TimeZone.getDefault())
    }

    val formattedSoftDeadline: String by lazy {
        DateTimeHelper.getPrintableOfIsoDate(softDeadline, datePattern, TimeZone.getDefault())
    }

    val formattedHardDeadline: String by lazy {
        DateTimeHelper.getPrintableOfIsoDate(hardDeadline, datePattern, TimeZone.getDefault())
    }


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeLong(course)
        dest.writeLongArray(units)
        dest.writeInt(position)
        dest.writeString(progress)
        dest.writeString(title)
        dest.writeString(slug)
        dest.writeString(beginDate)
        dest.writeString(endDate)
        dest.writeString(softDeadline)
        dest.writeString(hardDeadline)
        dest.writeString(gradingPolicy)
        dest.writeByte(if (isActive) 1.toByte() else 0.toByte())
        dest.writeString(createDate)
        dest.writeString(updateDate)
        dest.writeByte(if (isCached) 1.toByte() else 0.toByte())
        dest.writeByte(if (isLoading) 1.toByte() else 0.toByte())
        dest.writeParcelable(actions, flags)
        dest.writeInt(discountingPolicy?.ordinal ?: -1)
        dest.writeByte(if (isRequirementSatisfied) 1.toByte() else 0.toByte())
        dest.writeLong(requiredSection)
        dest.writeInt(requiredPercent)
    }

    protected constructor(input: Parcel) : this() {
        id = input.readLong()
        course = input.readLong()
        units = input.createLongArray()
        position = input.readInt()
        progress = input.readString()
        title = input.readString()
        slug = input.readString()
        beginDate = input.readString()
        endDate = input.readString()
        softDeadline = input.readString()
        hardDeadline = input.readString()
        gradingPolicy = input.readString()
        isActive = input.readByte().toInt() != 0
        createDate = input.readString()
        updateDate = input.readString()
        isCached = input.readByte().toInt() != 0
        isLoading = input.readByte().toInt() != 0
        actions = input.readParcelable(org.stepik.android.model.actions.Actions::class.java.classLoader)
        discountingPolicy = getDiscountingPolicyTypeByParcel(input)
        isRequirementSatisfied = input.readByte().toInt() != 0
        requiredSection = input.readLong()
        requiredPercent = input.readInt()
    }

    companion object {
        val datePattern = "dd MMMM yyyy HH:mm" //todo transfer to viewmodel/helper, keep section more plain

        @JvmField
        val CREATOR: Parcelable.Creator<Section> = object : Parcelable.Creator<Section> {
            override fun createFromParcel(input: Parcel): Section = Section(input)

            override fun newArray(size: Int): Array<Section?> = arrayOfNulls(size)
        }

        private fun getDiscountingPolicyTypeByParcel(input: Parcel): DiscountingPolicyType? {
            val temp = input.readInt()
            val localValues = DiscountingPolicyType.values()
            return if (temp >= 0 && temp < localValues.size) {
                localValues[temp]
            } else {
                null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Section

        if (id != other.id) return false
        if (course != other.course) return false
        if (!Arrays.equals(units, other.units)) return false
        if (position != other.position) return false
        if (progress != other.progress) return false
        if (title != other.title) return false
        if (slug != other.slug) return false
        if (beginDate != other.beginDate) return false
        if (endDate != other.endDate) return false
        if (softDeadline != other.softDeadline) return false
        if (hardDeadline != other.hardDeadline) return false
        if (gradingPolicy != other.gradingPolicy) return false
        if (isActive != other.isActive) return false
        if (createDate != other.createDate) return false
        if (updateDate != other.updateDate) return false
        if (actions != other.actions) return false
        if (isExam != other.isExam) return false
        if (discountingPolicy != other.discountingPolicy) return false
        if (isRequirementSatisfied != other.isRequirementSatisfied) return false
        if (requiredSection != other.requiredSection) return false
        if (requiredPercent != other.requiredPercent) return false
        if (isCached != other.isCached) return false
        if (isLoading != other.isLoading) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + course.hashCode()
        result = 31 * result + Arrays.hashCode(units)
        result = 31 * result + position
        result = 31 * result + (progress?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (beginDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (softDeadline?.hashCode() ?: 0)
        result = 31 * result + (hardDeadline?.hashCode() ?: 0)
        result = 31 * result + (gradingPolicy?.hashCode() ?: 0)
        result = 31 * result + isActive.hashCode()
        result = 31 * result + (createDate?.hashCode() ?: 0)
        result = 31 * result + (updateDate?.hashCode() ?: 0)
        result = 31 * result + (actions?.hashCode() ?: 0)
        result = 31 * result + isExam.hashCode()
        result = 31 * result + (discountingPolicy?.hashCode() ?: 0)
        result = 31 * result + isRequirementSatisfied.hashCode()
        result = 31 * result + requiredSection.hashCode()
        result = 31 * result + requiredPercent
        result = 31 * result + isCached.hashCode()
        result = 31 * result + isLoading.hashCode()
        return result
    }

}
