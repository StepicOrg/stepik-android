package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

enum class StepStatus : Parcelable {

    @SerializedName("ready")
    READY,
    @SerializedName("preparing")
    PREPARING,
    @SerializedName("error")
    ERROR;

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StepStatus> {
        override fun createFromParcel(parcel: Parcel): StepStatus = StepStatus.values()[parcel.readInt()]

        override fun newArray(size: Int): Array<StepStatus?> = arrayOfNulls(size)
    }

    object Helper {
        fun byName(serverName: String): StepStatus? =
                values().find {
                    it.name.equals(serverName, ignoreCase = true)
                }
    }
}
