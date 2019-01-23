package org.stepik.android.view.video_player.model

import android.os.Parcel
import android.os.Parcelable

class VideoPlayerData(
    val videoId: Long,
    val videoUrl: String,
    val startPosition: Long = 0,
    val mediaData: VideoPlayerMediaData
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(videoId)
        parcel.writeString(videoUrl)
        parcel.writeLong(startPosition)
        parcel.writeParcelable(mediaData, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VideoPlayerData> {
        override fun createFromParcel(parcel: Parcel): VideoPlayerData =
            VideoPlayerData(
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readLong(),
                parcel.readParcelable(VideoPlayerMediaData::class.java.classLoader)!!
            )

        override fun newArray(size: Int): Array<VideoPlayerData?> =
            arrayOfNulls(size)
    }
}