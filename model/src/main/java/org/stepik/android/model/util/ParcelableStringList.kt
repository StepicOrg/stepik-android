package org.stepik.android.model.util

import android.os.Parcel
import android.os.Parcelable

class ParcelableStringList() : ArrayList<String>(), Parcelable {
    private constructor (parcel: Parcel) : this() {
        parcel.readList(this, String::class.java.classLoader)
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(this)
    }

    companion object CREATOR : Parcelable.Creator<ParcelableStringList> {
        override fun createFromParcel(parcel: Parcel): ParcelableStringList = ParcelableStringList(parcel)
        override fun newArray(size: Int): Array<ParcelableStringList?> = arrayOfNulls(size)
    }
}
