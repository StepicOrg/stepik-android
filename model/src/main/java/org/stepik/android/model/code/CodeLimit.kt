package org.stepik.android.model.code

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CodeLimit(
    @SerializedName("time")
    val time: Int, //in seconds
    @SerializedName("memory")
    val memory: Int //in Mb
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(time)
        parcel.writeInt(memory)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CodeLimit> {
        override fun createFromParcel(parcel: Parcel): CodeLimit =
            CodeLimit(
                parcel.readInt(),
                parcel.readInt()
            )

        override fun newArray(size: Int): Array<CodeLimit?> =
            arrayOfNulls(size)
    }
}
