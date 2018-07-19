package org.stepic.droid.model

import org.stepik.android.model.learning.Submission

data class LessonSession(
        val stepId: Long,
        val attempt: org.stepik.android.model.learning.attempts.Attempt?,
        val submission: Submission?,
        val numberOfSubmissionsOnFirstPage: Int
)