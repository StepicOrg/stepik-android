package org.stepic.droid.events.attempts

import org.stepic.droid.model.Attempt

class SuccessAttemptEvent(stepId: Long, attempt: Attempt?, val isJustCreated: Boolean) : AttemptBaseEvent(stepId, attempt)