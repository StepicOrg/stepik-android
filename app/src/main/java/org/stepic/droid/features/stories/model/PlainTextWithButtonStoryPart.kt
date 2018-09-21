package org.stepic.droid.features.stories.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.StoryPart

class PlainTextWithButtonStoryPart(
        duration: Long,
        image: String,

        val button: StoryTemplate.Button?,
        val text: StoryTemplate.Text?
): StoryPart(duration, image) {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeString(cover)
        parcel.writeParcelable(button, flags)
        parcel.writeParcelable(text, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PlainTextWithButtonStoryPart> {
        override fun createFromParcel(parcel: Parcel) = PlainTextWithButtonStoryPart(
                parcel.readLong(),
                parcel.readString(),
                parcel.readParcelable(StoryTemplate.Button::class.java.classLoader),
                parcel.readParcelable(StoryTemplate.Text::class.java.classLoader)
        )

        override fun newArray(size: Int) =
                arrayOfNulls<PlainTextWithButtonStoryPart>(size)
    }
}