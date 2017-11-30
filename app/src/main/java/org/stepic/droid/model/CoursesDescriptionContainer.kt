package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

data class CoursesDescriptionContainer(
        val description: String,
        val colors: CollectionDescriptionColors
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeParcelable(colors, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CoursesDescriptionContainer> {
        override fun createFromParcel(parcel: Parcel) = CoursesDescriptionContainer(
                parcel.readString(),
                parcel.readParcelable(CollectionDescriptionColors::class.java.classLoader))

        override fun newArray(size: Int): Array<CoursesDescriptionContainer?>
                = arrayOfNulls(size)
    }
}