package org.stepic.droid.web.model.story_templates

import com.google.gson.annotations.SerializedName
import org.stepic.droid.web.MetaResponseBase
import org.stepik.android.model.Meta
import org.stepik.android.model.StoryTemplate

class StoryTemplatesResponse(
        meta: Meta,

        @SerializedName("story-templates")
        val storyTemplates: List<StoryTemplate>
) : MetaResponseBase(meta)