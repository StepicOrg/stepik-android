package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

class Actions() : Parcelable {

    var vote: Boolean? = null
    var delete: Boolean? = null
    var test_section: String? = null

    constructor(vote: Boolean?, delete: Boolean?, test_section: String?) : this() {
        this.vote = vote
        this.delete = delete
        this.test_section = test_section
    }


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (vote ?: false) 1.toByte() else 0.toByte())
        dest.writeByte(if (delete ?: false) 1.toByte() else 0.toByte())
        dest.writeString(test_section)
    }

    protected constructor(input: Parcel) : this(
            input.readByte().toInt() != 0,
            input.readByte().toInt() != 0,
            input.readString()
    )

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Actions> = object : Parcelable.Creator<Actions> {
            override fun createFromParcel(source: Parcel): Actions {
                return Actions(source)
            }

            override fun newArray(size: Int): Array<Actions?> {
                return arrayOfNulls(size)
            }
        }
    }

}