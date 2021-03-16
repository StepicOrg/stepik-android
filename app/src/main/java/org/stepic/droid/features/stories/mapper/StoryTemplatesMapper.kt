package org.stepic.droid.features.stories.mapper

import org.stepic.droid.features.stories.model.PlainTextWithButtonStoryPart
import org.stepic.droid.features.stories.model.FeedbackStoryPart
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart

private const val TYPE_TEXT = "text"
private const val TYPE_FEEDBACK = "feedback"


fun StoryTemplate.toStory(): Story =
        Story(
                id,
                title,
                cover,
                parts.map(StoryTemplate.Part::toStoryPart)
        )

fun StoryTemplate.Part.toStoryPart(): StoryPart = when (type) {
        TYPE_TEXT ->
                PlainTextWithButtonStoryPart(
                        duration * 1000, // convert seconds to ms
                        image,
                        button,
                        text
                )
        TYPE_FEEDBACK ->
                FeedbackStoryPart(
                        duration * 1000,
                        image,
                        feedback,
                        button,
                        text
                )
        else ->
                throw IllegalArgumentException("Unsupported story part type")
}
