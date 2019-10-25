package org.stepik.android.model.attempts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Pair(
    @SerializedName("first")
    val first: String?,
    @SerializedName("second")
    val second: String?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(first)
        parcel.writeString(second)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Pair> {
        override fun createFromParcel(parcel: Parcel): Pair =
            Pair(
                parcel.readString(),
                parcel.readString()
            )

        override fun newArray(size: Int): Array<Pair?> =
            arrayOfNulls(size)
    }
}
