package org.stepic.droid.web

import org.stepik.android.model.attempts.Attempt

class AttemptRequest(
        val attempt: Attempt
) {
    constructor(stepId: Long): this(Attempt(step = stepId))
}
