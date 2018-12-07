package org.stepic.droid.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

class LessonSession(
        val stepId: Long,
        val attempt: Attempt?,
        val submission: Submission?,
        val numberOfSubmissionsOnFirstPage: Int
)