package org.stepic.droid.core

import org.stepic.droid.model.Attempt
import org.stepic.droid.model.LessonSession
import org.stepic.droid.model.Submission
import java.util.*
import javax.inject.Inject

class LocalLessonSessionManager
@Inject constructor() : ILessonSessionManager {

    private val stepIdToLessonSession: MutableMap<Long, LessonSession>

    init {
        stepIdToLessonSession = HashMap<Long, LessonSession>()
    }

    override fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?, numberOfSubmissionOnFirstPage: Int) {
        if (attempt == null || submission == null) {
            return
        }

        stepIdToLessonSession.put(stepId, LessonSession(stepId, attempt, submission, numberOfSubmissionOnFirstPage))
    }

    override fun reset() {
        stepIdToLessonSession.clear()
    }

    override fun restoreLessonSession(stepId: Long): LessonSession? {
        return stepIdToLessonSession[stepId]
    }
}
