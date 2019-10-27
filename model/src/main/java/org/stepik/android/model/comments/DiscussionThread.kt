package org.stepik.android.model.comments

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class DiscussionThread(
    @SerializedName("id")
    val id: String,

    @SerializedName("thread")
    val thread: String,

    @SerializedName("discussions_count")
    val discussionsCount: Int,

    @SerializedName("discussion_proxy")
    val discussionProxy: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(thread)
        parcel.writeInt(discussionsCount)
        parcel.writeString(discussionProxy)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DiscussionThread> {
        const val THREAD_DEFAULT = "default"
        const val THREAD_SOLUTIONS = "solutions"

        override fun createFromParcel(parcel: Parcel): DiscussionThread =
            DiscussionThread(
                parcel.readString()!!,
                parcel.readString()!!,
                parcel.readInt(),
                parcel.readString()!!
            )

        override fun newArray(size: Int): Array<DiscussionThread?> =
            arrayOfNulls(size)
    }
}