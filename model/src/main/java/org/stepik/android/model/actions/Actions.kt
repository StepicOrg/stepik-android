package org.stepik.android.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Actions(
        val vote: Boolean = false,
        val delete: Boolean = false,
        @SerializedName("test_section") val testSection: String? = null,

        @SerializedName("do_review") val doReview: String? = null,
        @SerializedName("edit_instructions") val editInstructions: String? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (vote) 1 else 0)
        parcel.writeByte(if (delete) 1 else 0)
        parcel.writeString(testSection)
        parcel.writeString(doReview)
        parcel.writeString(editInstructions)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Actions> {
        override fun createFromParcel(parcel: Parcel): Actions = Actions(
                parcel.readByte() != 0.toByte(),
                parcel.readByte() != 0.toByte(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
        )

        override fun newArray(size: Int): Array<Actions?> = arrayOfNulls(size)
    }
}