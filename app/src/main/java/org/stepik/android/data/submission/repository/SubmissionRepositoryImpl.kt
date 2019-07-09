package org.stepik.android.data.submission.repository

import io.reactivex.Single
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import javax.inject.Inject

class SubmissionRepositoryImpl
@Inject
constructor(
    private val submissionRemoteDataSource: SubmissionRemoteDataSource
) : SubmissionRepository {
    override fun createSubmission(submission: Submission): Single<Submission> =
        submissionRemoteDataSource.createSubmission(submission)

    override fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>> =
        submissionRemoteDataSource.getSubmissionsForAttempt(attemptId)

    override fun getSubmissionsForStep(stepId: Long): Single<List<Submission>> =
        submissionRemoteDataSource.getSubmissionsForStep(stepId)
}