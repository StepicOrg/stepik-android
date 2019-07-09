package org.stepik.android.data.submission.source

import io.reactivex.Single
import org.stepik.android.model.Submission

interface SubmissionRemoteDataSource {
    fun createSubmission(submission: Submission): Single<Submission>
    fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>>
    fun getSubmissionsForStep(stepId: Long): Single<List<Submission>>
}