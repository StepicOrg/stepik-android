package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable

data class CoursesDescriptionContainer(
        var description: String,
        var colors: CollectionDescriptionColors
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(CollectionDescriptionColors::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
        parcel.writeParcelable(colors, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CoursesDescriptionContainer> {
        override fun createFromParcel(parcel: Parcel): CoursesDescriptionContainer
                = CoursesDescriptionContainer(parcel)

        override fun newArray(size: Int): Array<CoursesDescriptionContainer?>
                = arrayOfNulls(size)
    }
}