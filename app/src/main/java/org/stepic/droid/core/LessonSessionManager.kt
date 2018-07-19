package org.stepic.droid.core

import org.stepik.android.model.attempts.Attempt
import org.stepic.droid.model.LessonSession
import org.stepik.android.model.Submission

interface LessonSessionManager {

    fun restoreLessonSession(stepId: Long): LessonSession?

    fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?, numberOfSubmissionOnFirstPage: Int)

    fun reset()
}
