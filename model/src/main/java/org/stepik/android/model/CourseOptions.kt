package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CourseOptions(
    @SerializedName("course_preview")
    val coursePreview: CoursePreview?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(coursePreview, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CourseOptions> {
        override fun createFromParcel(parcel: Parcel): CourseOptions =
            CourseOptions(
                parcel.readParcelable(CoursePreview::class.java.classLoader)
            )

        override fun newArray(size: Int): Array<CourseOptions?> =
            arrayOfNulls(size)
    }
}