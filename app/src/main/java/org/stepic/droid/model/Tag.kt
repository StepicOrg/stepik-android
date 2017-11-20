package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

data class Tag(
        val id: Int,
        val title: String
) : Parcelable {
    private constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag = Tag(parcel)

        override fun newArray(size: Int): Array<Tag?> = arrayOfNulls(size)
    }
}