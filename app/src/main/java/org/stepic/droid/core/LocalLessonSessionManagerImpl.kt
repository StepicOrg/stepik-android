package org.stepic.droid.core

import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.di.AppCoreModule
import org.stepik.android.model.learning.attempts.Attempt
import org.stepic.droid.model.LessonSession
import org.stepic.droid.model.Submission
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.storage.operations.DatabaseFacade
import javax.inject.Inject
import javax.inject.Named

class LocalLessonSessionManagerImpl
@Inject constructor(
        //single thread for avoiding race condition and creation of tons of threads
        @Named(AppCoreModule.SINGLE_THREAD_CODE_SAVER)
        private val singleThreadExecutor: SingleThreadExecutor,
        private val databaseFacade: DatabaseFacade
) : LessonSessionManager {

    private val stepIdToLessonSession: MutableMap<Long, LessonSession> = hashMapOf()

    override fun saveSession(stepId: Long, attempt: Attempt?, submission: Submission?, numberOfSubmissionOnFirstPage: Int) {
        if (attempt == null || submission == null) {
            return
        }

        stepIdToLessonSession[stepId] = LessonSession(stepId, attempt, submission, numberOfSubmissionOnFirstPage)

        if (submission.reply?.language != null && submission.reply?.code != null && submission.status == Submission.Status.LOCAL) {
            singleThreadExecutor.execute {
                val codeSubmission = CodeSubmission(
                        stepId = stepId,
                        attemptId = attempt.id,
                        code = submission.reply.code,
                        language = submission.reply.language
                )
                databaseFacade.addCodeSubmission(codeSubmission)
            }
        }
    }

    override fun reset() {
        stepIdToLessonSession.clear()
    }

    override fun restoreLessonSession(stepId: Long): LessonSession? = stepIdToLessonSession[stepId]
}
