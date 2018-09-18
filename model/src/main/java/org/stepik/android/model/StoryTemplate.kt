package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class StoryTemplate(
        val id: Long,
        val cover: String,
        val title: String,

        @SerializedName("is_published")
        val isPublished: Boolean,

        val parts: List<Part>,

        val language: String,
        val position: Int,
        val version: Int
) {
    data class Part(
            val duration: Long,
            val image: String,
            val position: Int,
            val type: String,

            val button: Button?,
            val text: Text?
    )

    data class Text(
            @SerializedName("background_style")
            val backgroundStyle: String,

            @SerializedName("text")
            val text: String,

            @SerializedName("text_color")
            val textColor: String,

            @SerializedName("title")
            val title: String
    ) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(backgroundStyle)
            parcel.writeString(text)
            parcel.writeString(textColor)
            parcel.writeString(title)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Text> {
            override fun createFromParcel(parcel: Parcel) = Text(
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString()
            )

            override fun newArray(size: Int) =
                    arrayOfNulls<Text?>(size)
        }
    }

    data class Button(
            @SerializedName("background_color")
            val backgroundColor: String,

            @SerializedName("text_color")
            val textColor: String,

            @SerializedName("title")
            val title: String,

            @SerializedName("url")
            val url: String
    ) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(backgroundColor)
            parcel.writeString(textColor)
            parcel.writeString(title)
            parcel.writeString(url)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Button> {
            override fun createFromParcel(parcel: Parcel) = Button(
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString()
            )

            override fun newArray(size: Int) =
                    arrayOfNulls<Button?>(size)
        }
    }
}