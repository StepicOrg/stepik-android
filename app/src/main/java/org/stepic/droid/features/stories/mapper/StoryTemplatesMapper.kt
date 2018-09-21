package org.stepic.droid.features.stories.mapper

import org.stepic.droid.features.stories.model.PlainTextWithButtonStoryPart
import org.stepik.android.model.StoryTemplate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.model.StoryPart

fun StoryTemplate.toStory(): Story =
        Story(
                id,
                title,
                cover,
                parts.map(StoryTemplate.Part::toStoryPart)
        )

fun StoryTemplate.Part.toStoryPart(): StoryPart =
        PlainTextWithButtonStoryPart(
                duration * 1000, // convert seconds to ms
                image,
                button,
                text
        )