package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class LessonActions(
    @SerializedName("learn_lesson")
    val learnLesson: String?,

    @SerializedName("assist_lesson")
    val assistLesson: String?,
    @SerializedName("view_all_submissions")
    val viewAllSubmissions: String?,

    @SerializedName("edit_lesson")
    val editLesson: String?,
    @SerializedName("view_statistics")
    val viewStatistics: String?,
    @SerializedName("attachments")
    val attachments: String?,
    @SerializedName("clone_lesson")
    val cloneLesson: String?,

    @SerializedName("edit_permissions")
    val editPermissions: String?,
    @SerializedName("delete_lesson")
    val deleteLesson: String?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(learnLesson)
        parcel.writeString(assistLesson)
        parcel.writeString(viewAllSubmissions)
        parcel.writeString(editLesson)
        parcel.writeString(viewStatistics)
        parcel.writeString(attachments)
        parcel.writeString(cloneLesson)
        parcel.writeString(editPermissions)
        parcel.writeString(deleteLesson)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LessonActions> {
        override fun createFromParcel(parcel: Parcel): LessonActions =
            LessonActions(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
            )

        override fun newArray(size: Int): Array<LessonActions?> =
            arrayOfNulls(size)
    }
}