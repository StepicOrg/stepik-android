package org.stepik.android.view.video_player.model

import android.os.Parcel
import android.os.Parcelable
import org.stepic.droid.preferences.VideoPlaybackRate

data class VideoPlayerData(
    val videoId: Long,
    val videoUrl: String,
    val videoPlaybackRate: VideoPlaybackRate,
    val videoTimestamp: Long = 0,
    val mediaData: VideoPlayerMediaData,
    val videoQuality: String
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(videoId)
        parcel.writeString(videoUrl)
        parcel.writeInt(videoPlaybackRate.ordinal)
        parcel.writeLong(videoTimestamp)
        parcel.writeParcelable(mediaData, flags)
        parcel.writeString(videoQuality)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VideoPlayerData> {
        override fun createFromParcel(parcel: Parcel): VideoPlayerData =
            VideoPlayerData(
                parcel.readLong(),
                parcel.readString()!!,
                VideoPlaybackRate.values()[parcel.readInt()],
                parcel.readLong(),
                parcel.readParcelable(VideoPlayerMediaData::class.java.classLoader)!!,
                parcel.readString()!!
            )

        override fun newArray(size: Int): Array<VideoPlayerData?> =
            arrayOfNulls(size)
    }
}