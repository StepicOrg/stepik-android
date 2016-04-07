package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

class Step : Parcelable, Serializable {
    var id: Long = 0
    var lesson: Long = 0
    var position: Long = 0
    var status: String? = null
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
    var actions: ActionsContainer? = null

    constructor()

    override fun describeContents(): Int {
        val step = Step()
        step.block?.video?.urls?.size
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeLong(this.lesson)
        dest.writeLong(this.position)
        dest.writeString(this.status)
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
        this.status = input.readString()
        this.block = input.readParcelable<Block>(Block::class.java.classLoader)
        this.progress = input.readString()
        this.subscriptions = input.createStringArray()
        this.viewed_by = input.readLong()
        this.passed_by = input.readLong()
        this.create_date = input.readString()
        this.update_date = input.readString()
        this.is_cached = input.readByte().toInt() != 0
        this.is_loading = input.readByte().toInt() != 0
        this.is_custom_passed = input.readByte().toInt() != 0
        this.actions = input.readParcelable<ActionsContainer>(ActionsContainer::class.java.classLoader)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Step> = object : Parcelable.Creator<Step> {
            override fun createFromParcel(source: Parcel): Step? {
                return Step(source)
            }

            override fun newArray(size: Int): Array<out Step?> {
                return arrayOfNulls(size)
            }
        }
    }
}
