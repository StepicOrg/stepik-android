package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Video(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("thumbnail")
    val thumbnail: String? = null,
    @SerializedName("urls")
    val urls: List<VideoUrl> = emptyList(),
    @SerializedName("duration")
    val duration: Long = 0
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(thumbnail)
        parcel.writeTypedList(urls)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Video> {
        override fun createFromParcel(parcel: Parcel) =
            Video(
                parcel.readLong(),
                parcel.readString(),
                parcel.createTypedArrayList(VideoUrl),
                parcel.readLong()
            )

        override fun newArray(size: Int): Array<Video?> =
            arrayOfNulls(size)
    }
}


data class VideoUrl(
    @SerializedName("url")
    val url: String?,
    @SerializedName("quality")
    val quality: String?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(quality)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VideoUrl> {
        override fun createFromParcel(parcel: Parcel) =
            VideoUrl(parcel.readString(), parcel.readString())

        override fun newArray(size: Int): Array<VideoUrl?> =
            arrayOfNulls(size)
    }
}