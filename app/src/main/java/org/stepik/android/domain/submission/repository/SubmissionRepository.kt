package org.stepik.android.domain.submission.repository

import io.reactivex.Single
import org.stepik.android.model.Submission

interface SubmissionRepository {
    fun createSubmission(submission: Submission): Single<Submission>
    fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>>
    fun getSubmissionsForStep(stepId: Long): Single<List<Submission>>
}