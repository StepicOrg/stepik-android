package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.structure.Actions
import org.stepik.android.model.structure.Block

import java.io.Serializable

class Step : Parcelable, Serializable, IProgressable {
    override fun getProgressId(): String? = progress

    var id: Long = 0
    var lesson: Long = 0
    var position: Long = 0
    var status: StepStatus? = null
    var block: Block? = null
    var progress: String? = null
    var subscriptions: Array<String>? = null
    var viewed_by: Long = 0
    var passed_by: Long = 0
    var create_date: String? = null
    var update_date: String? = null
    var is_cached: Boolean = false
    var is_loading: Boolean = false
    var is_custom_passed: Boolean = false
    var actions: Actions? = null
    var discussions_count: Int = 0
    var discussion_proxy: String? = null
    @SerializedName("has_submissions_restrictions")
    var hasSubmissionRestriction = false
    @SerializedName("max_submissions_count")
    var maxSubmissionCount: Int = 0

    constructor()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.lesson)
        dest.writeLong(this.position)
        dest.writeInt(this.discussions_count)
        dest.writeString(this.discussion_proxy)
        dest.writeParcelable(this.status, flags)
        dest.writeParcelable(this.block, 0)
        dest.writeString(this.progress)
        dest.writeStringArray(this.subscriptions)
        dest.writeLong(this.viewed_by)
        dest.writeLong(this.passed_by)
        dest.writeString(this.create_date)
        dest.writeString(this.update_date)
        dest.writeByte(if (is_cached) 1.toByte() else 0.toByte())
        dest.writeByte(if (is_loading) 1.toByte() else 0.toByte())
        dest.writeByte(if (is_custom_passed) 1.toByte() else 0.toByte())
        dest.writeParcelable(this.actions, flags)
    }

    protected constructor(input: Parcel) {
        this.id = input.readLong()
        this.lesson = input.readLong()
        this.position = input.readLong()
        this.discussions_count = input.readInt()
        this.discussion_proxy = input.readString()
        this.status = input.readParcelable(StepStatus::class.java.classLoader)
        this.block = input.readParcelable(Block::class.java.classLoader)
        this.progress = input.readString()
        this.subscriptions = input.createStringArray()
        this.viewed_by = input.readLong()
        this.passed_by = input.readLong()
        this.create_date = input.readString()
        this.update_date = input.readString()
        this.is_cached = input.readByte().toInt() != 0
        this.is_loading = input.readByte().toInt() != 0
        this.is_custom_passed = input.readByte().toInt() != 0
        this.actions = input.readParcelable(Actions::class.java.classLoader)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Step> = object : Parcelable.Creator<Step> {
            override fun createFromParcel(source: Parcel): Step? = Step(source)

            override fun newArray(size: Int): Array<out Step?> = arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Step

        if (id != other.id) return false
        if (lesson != other.lesson) return false
        if (position != other.position) return false
        if (status != other.status) return false
        if (progress != other.progress) return false
        if (viewed_by != other.viewed_by) return false
        if (passed_by != other.passed_by) return false
        if (create_date != other.create_date) return false
        if (update_date != other.update_date) return false
        if (is_cached != other.is_cached) return false
        if (is_loading != other.is_loading) return false
        if (is_custom_passed != other.is_custom_passed) return false
        if (discussions_count != other.discussions_count) return false
        if (discussion_proxy != other.discussion_proxy) return false
        if (hasSubmissionRestriction != other.hasSubmissionRestriction) return false
        if (maxSubmissionCount != other.maxSubmissionCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + lesson.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + (status?.hashCode() ?: 0)
        result = 31 * result + (progress?.hashCode() ?: 0)
        result = 31 * result + viewed_by.hashCode()
        result = 31 * result + passed_by.hashCode()
        result = 31 * result + (create_date?.hashCode() ?: 0)
        result = 31 * result + (update_date?.hashCode() ?: 0)
        result = 31 * result + is_cached.hashCode()
        result = 31 * result + is_loading.hashCode()
        result = 31 * result + is_custom_passed.hashCode()
        result = 31 * result + discussions_count
        result = 31 * result + (discussion_proxy?.hashCode() ?: 0)
        result = 31 * result + hasSubmissionRestriction.hashCode()
        result = 31 * result + maxSubmissionCount
        return result
    }

}
