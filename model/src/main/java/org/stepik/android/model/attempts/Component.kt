package org.stepik.android.model.attempts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Component(
    @SerializedName("type")
    val type: String,
    @SerializedName("text")
    val text: String?,
    @SerializedName("options")
    val options: List<String>?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(text)
        parcel.writeList(options)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Component> {
        override fun createFromParcel(parcel: Parcel): Component =
            Component(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.createStringArrayList()
            )

        override fun newArray(size: Int): Array<Component?> =
            arrayOfNulls(size)
    }
}