package org.stepic.droid.model

data class LessonSession(val stepId: Long,
                         val attempt: Attempt?,
                         val submission: Submission?,
                         val numberOfSubmissionsOnFirstPage: Int)