package org.stepik.android.remote.attempt.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.attempts.Attempt

class AttemptRequest(
    @SerializedName("attempt")
    val attempt: Attempt
) {
    constructor(stepId: Long) : this(Attempt(step = stepId))
}
