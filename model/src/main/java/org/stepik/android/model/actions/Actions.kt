package org.stepik.android.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Actions(
        val vote: Boolean,
        val delete: Boolean,
        @SerializedName("test_section") val testSection: String?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (vote) 1 else 0)
        parcel.writeByte(if (delete) 1 else 0)
        parcel.writeString(testSection)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Actions> {
        override fun createFromParcel(parcel: Parcel): Actions = Actions(
                parcel.readByte() != 0.toByte(),
                parcel.readByte() != 0.toByte(),
                parcel.readString()
        )

        override fun newArray(size: Int): Array<Actions?> = arrayOfNulls(size)
    }
}