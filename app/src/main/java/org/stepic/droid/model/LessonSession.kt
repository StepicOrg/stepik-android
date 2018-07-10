package org.stepic.droid.model

data class LessonSession(
        val stepId: Long,
        val attempt: org.stepik.android.model.learning.attempts.Attempt?,
        val submission: Submission?,
        val numberOfSubmissionsOnFirstPage: Int
)