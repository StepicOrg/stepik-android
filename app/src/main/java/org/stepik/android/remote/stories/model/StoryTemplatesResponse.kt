package org.stepik.android.remote.stories.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.StoryTemplate
import org.stepik.android.remote.base.model.MetaResponse

class StoryTemplatesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("story-templates")
    val storyTemplates: List<StoryTemplate>
) : MetaResponse