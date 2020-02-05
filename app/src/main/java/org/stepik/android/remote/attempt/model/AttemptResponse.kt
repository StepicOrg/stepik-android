package org.stepik.android.remote.attempt.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.base.model.MetaResponse

class AttemptResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("solutions")
    val attempts: List<Attempt>
) : MetaResponse
