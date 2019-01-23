package org.stepik.android.view.video_player.model

import android.os.Parcel
import android.os.Parcelable

class VideoPlayerData(
    val videoId: Long,
    val thumbnail: String? = null,
    val title: String,
    val description: String? = null,
    val videoUrl: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(videoId)
        parcel.writeString(thumbnail)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(videoUrl)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VideoPlayerData> {
        override fun createFromParcel(parcel: Parcel): VideoPlayerData =
            VideoPlayerData(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString()!!,
                parcel.readString(),
                parcel.readString()!!
            )

        override fun newArray(size: Int): Array<VideoPlayerData?> =
            arrayOfNulls(size)
    }
}