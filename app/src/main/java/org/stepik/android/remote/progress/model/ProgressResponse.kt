package org.stepik.android.remote.progress.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Progress
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class ProgressResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("progresses")
    val progresses: List<Progress>
) : MetaResponse