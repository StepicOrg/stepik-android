package org.stepik.android.remote.submission.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.Submission
import org.stepik.android.remote.base.model.MetaResponse

class SubmissionResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("submissions")
    val submissions: List<Submission>
) : MetaResponse