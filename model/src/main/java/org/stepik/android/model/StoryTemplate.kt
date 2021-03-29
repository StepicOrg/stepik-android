package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class StoryTemplate(
        @SerializedName("id")
        val id: Long,
        @SerializedName("cover")
        val cover: String,
        @SerializedName("title")
        val title: String,

        @SerializedName("is_published")
        val isPublished: Boolean,

        @SerializedName("parts")
        val parts: List<Part>,

        @SerializedName("language")
        val language: String,
        @SerializedName("position")
        val position: Int,
        @SerializedName("version")
        val version: Int
) {
    class Part(
            @SerializedName("duration")
            val duration: Long,
            @SerializedName("image")
            val image: String,
            @SerializedName("position")
            val position: Int,
            @SerializedName("type")
            val type: String,

            @SerializedName("feedback")
            val feedback: Feedback?,
            @SerializedName("button")
            val button: Button?,
            @SerializedName("text")
            val text: Text?
    )

    class Text(
            @SerializedName("background_style")
            val backgroundStyle: String?,

            @SerializedName("text")
            val text: String?,

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
                    parcel.readString()!!,
                    parcel.readString()!!
            )

            override fun newArray(size: Int) =
                    arrayOfNulls<Text?>(size)
        }
    }

    class Button(
            @SerializedName("background_color")
            val backgroundColor: String,

            @SerializedName("text_color")
            val textColor: String,

            @SerializedName("title")
            val title: String,

            @SerializedName("url")
            val url: String,

            @SerializedName("feedback_title")
            val feedbackTitle: String?
    ) : Parcelable {
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(backgroundColor)
            parcel.writeString(textColor)
            parcel.writeString(title)
            parcel.writeString(url)
            parcel.writeString(feedbackTitle)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Button> {
            override fun createFromParcel(parcel: Parcel) = Button(
                    parcel.readString()!!,
                    parcel.readString()!!,
                    parcel.readString()!!,
                    parcel.readString()!!,
                    parcel.readString()
            )

            override fun newArray(size: Int) =
                    arrayOfNulls<Button?>(size)
        }
    }

    @Parcelize
    data class Feedback(
        @SerializedName("background_color")
        val backgroundColor: String,
        @SerializedName("text")
        val text: String,
        @SerializedName("text_color")
        val textColor: String,
        @SerializedName("icon_style")
        val iconStyle: IconStyle,
        @SerializedName("input_background_color")
        val inputBackgroundColor: String,
        @SerializedName("input_text_color")
        val inputTextColor: String,
        @SerializedName("placeholder_text")
        val placeholderText: String,
        @SerializedName("placeholder_text_color")
        val placeholderTextColor: String
    ) : Parcelable {
        enum class IconStyle(val style: String) {
            @SerializedName("light")
            LIGHT("light"),
            @SerializedName("dark")
            DARK("dark")
        }
    }
}