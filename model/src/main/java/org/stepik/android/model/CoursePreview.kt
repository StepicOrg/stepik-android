package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CoursePreview(
    @SerializedName("preview_lesson_id")
    val previewLessonId: Long
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(previewLessonId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CoursePreview> {
        override fun createFromParcel(parcel: Parcel): CoursePreview =
            CoursePreview(
                parcel.readLong()
            )

        override fun newArray(size: Int): Array<CoursePreview?> =
            arrayOfNulls(size)
    }
}