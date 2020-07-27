package org.stepik.android.model.analytic

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonElement

data class AnalyticLocalEvent(
    val name: String,
    val eventData: JsonElement,
    val eventTimestamp: Long
) : Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeValue(eventData)
        dest.writeLong(eventTimestamp)
    }

    companion object CREATOR : Parcelable.Creator<AnalyticLocalEvent> {
        override fun createFromParcel(parcel: Parcel): AnalyticLocalEvent =
            AnalyticLocalEvent(
                parcel.readString() ?: "",
                parcel.readValue(JsonElement::class.java.classLoader) as JsonElement,
                parcel.readLong()
            )

        override fun newArray(size: Int): Array<AnalyticLocalEvent?> =
            arrayOfNulls(size)
    }
}