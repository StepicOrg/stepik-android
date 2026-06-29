package org.stepik.android.model

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StoryTemplateTest {
    @Test
    fun storyTextWithMissingTitleAndTextColorIsParcelable() {
        val storyText = StoryTemplate.Text(
            backgroundStyle = null,
            text = "Text",
            textColor = null,
            title = null
        )

        val restoredStoryText = storyText.parcelAndRestore()

        assertNull(restoredStoryText.backgroundStyle)
        assertEquals(storyText.text, restoredStoryText.text)
        assertNull(restoredStoryText.textColor)
        assertNull(restoredStoryText.title)
    }

    @Test
    fun storyTextWithTextColorIsParcelable() {
        val storyText = StoryTemplate.Text(
            backgroundStyle = null,
            text = "Text",
            textColor = "ffffff",
            title = "Title"
        )

        val restoredStoryText = storyText.parcelAndRestore()

        assertNull(restoredStoryText.backgroundStyle)
        assertEquals(storyText.text, restoredStoryText.text)
        assertEquals(storyText.textColor, restoredStoryText.textColor)
        assertEquals(storyText.title, restoredStoryText.title)
    }

    private fun StoryTemplate.Text.parcelAndRestore(): StoryTemplate.Text {
        val parcel = Parcel.obtain()

        try {
            writeToParcel(parcel, 0)
            parcel.setDataPosition(0)

            return StoryTemplate.Text.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}
