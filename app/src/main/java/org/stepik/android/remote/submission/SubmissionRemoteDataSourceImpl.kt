package org.stepik.android.remote.submission

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.util.first
import org.stepic.droid.web.Api
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.model.Submission
import org.stepik.android.remote.submission.model.SubmissionResponse
import javax.inject.Inject

class SubmissionRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : SubmissionRemoteDataSource {
    private val submissionMapper = Function(SubmissionResponse::submissions)

    override fun createSubmission(submission: Submission): Single<Submission> =
        api.createNewSubmissionReactive(submission)
            .map(submissionMapper)
            .first()

    override fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>> =
        api.getSubmissionsReactive(attemptId)
            .map(submissionMapper)

    override fun getSubmissionsForStep(stepId: Long): Single<List<Submission>> =
        api.getSubmissionForStepReactive(stepId)
            .map(submissionMapper)
}