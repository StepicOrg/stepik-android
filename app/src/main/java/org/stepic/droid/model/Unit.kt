package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

class Unit : Serializable, Parcelable, IProgressable {
    var id: Long = 0
    var section: Long = 0
    var lesson: Long = 0
    var assignments: LongArray? = null
    var position: Int = 0
    var begin_date: String? = null
    var end_date: String? = null
    var progress: String? = null
    var soft_deadline: String? = null
    var hard_deadline: String? = null
    var grading_policy: String? = null
    var begin_date_source: String? = null
    var end_date_source: String? = null
    var soft_deadline_source: String? = null
    var hard_deadline_source: String? = null
    var grading_policy_source: String? = null
    var is_active: Boolean = false
    var create_date: String? = null
    var update_date: String? = null

    @Deprecated("")
    var is_viewed_custom: Boolean = false

    override fun getProgressId(): String? = progress

    constructor() {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.section)
        dest.writeLong(this.lesson)
        dest.writeLongArray(this.assignments)
        dest.writeInt(this.position)
        dest.writeString(this.progress)
        dest.writeString(this.begin_date)
        dest.writeString(this.end_date)
        dest.writeString(this.soft_deadline)
        dest.writeString(this.hard_deadline)
        dest.writeString(this.grading_policy)
        dest.writeString(this.begin_date_source)
        dest.writeString(this.end_date_source)
        dest.writeString(this.soft_deadline_source)
        dest.writeString(this.hard_deadline_source)
        dest.writeString(this.grading_policy_source)
        dest.writeByte(if (is_active) 1.toByte() else 0.toByte())
        dest.writeString(this.create_date)
        dest.writeString(this.update_date)
        dest.writeByte(if (is_viewed_custom) 1.toByte() else 0.toByte())
    }

    protected constructor(input: Parcel) {
        this.id = input.readLong()
        this.section = input.readLong()
        this.lesson = input.readLong()
        this.assignments = input.createLongArray()
        this.position = input.readInt()
        this.progress = input.readString()
        this.begin_date = input.readString()
        this.end_date = input.readString()
        this.soft_deadline = input.readString()
        this.hard_deadline = input.readString()
        this.grading_policy = input.readString()
        this.begin_date_source = input.readString()
        this.end_date_source = input.readString()
        this.soft_deadline_source = input.readString()
        this.hard_deadline_source = input.readString()
        this.grading_policy_source = input.readString()
        this.is_active = input.readByte().toInt() != 0
        this.create_date = input.readString()
        this.update_date = input.readString()
        this.is_viewed_custom = input.readByte().toInt() != 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Unit> = object : Parcelable.Creator<Unit> {
            override fun createFromParcel(source: Parcel): Unit {
                return Unit(source)
            }

            override fun newArray(size: Int): Array<Unit?> {
                return arrayOfNulls(size)
            }
        }
    }
}
