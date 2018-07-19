package org.stepik.android.model.code

import android.os.Parcel
import android.os.Parcelable

data class CodeLimit(
        val time: Int, //in seconds
        val memory: Int //in Mb
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(time)
        parcel.writeInt(memory)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CodeLimit> {
        override fun createFromParcel(parcel: Parcel): CodeLimit = CodeLimit(
                parcel.readInt(),
                parcel.readInt()
        )

        override fun newArray(size: Int): Array<CodeLimit?> = arrayOfNulls(size)
    }
}
