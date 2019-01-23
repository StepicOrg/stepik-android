package org.stepik.android.view.video_player.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.Video

class VideoPlayerMediaData(
    val thumbnail: String? = null,
    val title: String,
    val description: String? = null,
    val cachedVideo: Video? = null,
    val externalVideo: Video? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(thumbnail)
        parcel.writeString(title)
        parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VideoPlayerMediaData> {
        override fun createFromParcel(parcel: Parcel): VideoPlayerMediaData =
            VideoPlayerMediaData(
                parcel.readString(),
                parcel.readString()!!,
                parcel.readString()
            )

        override fun newArray(size: Int): Array<VideoPlayerMediaData?> =
            arrayOfNulls(size)
    }
}