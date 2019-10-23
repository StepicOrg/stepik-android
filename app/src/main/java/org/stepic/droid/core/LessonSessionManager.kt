package org.stepic.droid.core

import org.stepic.droid.model.LessonSession
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

interface LessonSessionManager {

    fun restoreLessonSession(stepId: Long): LessonSession?

    fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?, numberOfSubmissionOnFirstPage: Int)

    fun reset()
}
