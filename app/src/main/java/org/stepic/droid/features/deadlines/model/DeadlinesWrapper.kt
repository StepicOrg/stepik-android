package org.stepic.droid.features.deadlines.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class DeadlinesWrapper(
    @SerializedName("course")
    val course: Long,
    @SerializedName("DeadlinesWrapper")
    val deadlines: List<Deadline>
): Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(course)
        parcel.writeTypedList(deadlines)
    }

    override fun describeContents(): Int = 0
    companion object CREATOR : Parcelable.Creator<DeadlinesWrapper> {
        override fun createFromParcel(parcel: Parcel) = DeadlinesWrapper(
                parcel.readLong(),
                parcel.createTypedArrayList(Deadline)!!
        )

        override fun newArray(size: Int): Array<DeadlinesWrapper?> = arrayOfNulls(size)
    }
}