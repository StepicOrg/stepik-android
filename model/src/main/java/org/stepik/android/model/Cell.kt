package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

data class Cell(
    @SerializedName("name")
    val name: String,
    @SerializedName("answer")
    var answer: Boolean
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeBoolean(answer)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Cell> {
        override fun createFromParcel(parcel: Parcel): Cell =
            Cell(
                parcel.readString()!!,
                parcel.readBoolean()
            )

        override fun newArray(size: Int): Array<Cell?> =
            arrayOfNulls(size)
    }
}