package org.stepic.droid.core

import org.stepic.droid.model.LessonSession
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class LocalLessonSessionManagerImpl
@Inject
constructor() : LessonSessionManager {

    private val stepIdToLessonSession: MutableMap<Long, LessonSession> = hashMapOf()

    override fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?, numberOfSubmissionOnFirstPage: Int) {
        if (attempt == null || submission == null) {
            return
        }

        stepIdToLessonSession[stepId] = LessonSession(stepId, attempt, submission, numberOfSubmissionOnFirstPage)
    }

    override fun reset() {
        stepIdToLessonSession.clear()
    }

    override fun restoreLessonSession(stepId: Long): LessonSession? = stepIdToLessonSession[stepId]
}
