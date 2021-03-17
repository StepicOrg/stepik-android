package org.stepic.droid.features.stories.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.StoryPart

class FeedbackStoryPart(
    duration: Long,
    image: String,

    val feedback: StoryTemplate.Feedback?,
    val button: StoryTemplate.Button?,
    val text: StoryTemplate.Text?
) : StoryPart(duration, image) {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeString(cover)
        parcel.writeParcelable(feedback, flags)
        parcel.writeParcelable(button, flags)
        parcel.writeParcelable(text, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FeedbackStoryPart> {
        override fun createFromParcel(parcel: Parcel): FeedbackStoryPart =
            FeedbackStoryPart(
                parcel.readLong(),
                parcel.readString()!!,
                parcel.readParcelable(StoryTemplate::Feedback::class.java.classLoader),
                parcel.readParcelable(StoryTemplate::Button::class.java.classLoader),
                parcel.readParcelable(StoryTemplate.Text::class.java.classLoader)
            )

        override fun newArray(size: Int): Array<FeedbackStoryPart?> =
            arrayOfNulls(size)
    }
}