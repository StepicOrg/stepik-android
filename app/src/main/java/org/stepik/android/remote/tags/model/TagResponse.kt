package org.stepik.android.remote.tags.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.Tag
import org.stepik.android.remote.base.model.MetaResponse

class TagResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("tags")
    val tags: List<Tag>
) : MetaResponse
