package org.stepik.android.remote.submission

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.util.PagedList
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.model.Submission
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.submission.model.SubmissionRequest
import org.stepik.android.remote.submission.model.SubmissionResponse
import org.stepik.android.remote.submission.service.SubmissionService
import ru.nobird.android.core.model.mapOfNotNull
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class SubmissionRemoteDataSourceImpl
@Inject
constructor(
    private val submissionService: SubmissionService
) : SubmissionRemoteDataSource {
    companion object {
        private const val STEP = "step"
        private const val USER = "user"
        private const val STATUS = "status"
        private const val PAGE = "page"
    }

    private val submissionMapper = Function { response: SubmissionResponse ->
        response.submissions.map { submission ->
            val isPartial = submission.status == Submission.Status.CORRECT && (submission.score?.toFloatOrNull() ?: 0f) < 1f
            if (isPartial) {
                submission.copy(status = Submission.Status.PARTIALLY_CORRECT, _reply = submission.reply)
            } else {
                submission
            }
        }
    }

    override fun createSubmission(submission: Submission): Single<Submission> =
        submissionService.createNewSubmission(SubmissionRequest(submission))
            .map(submissionMapper)
            .first()

    override fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>> =
        submissionService.getSubmissions(attemptId)
            .map(submissionMapper)

    override fun getSubmissionsForStep(stepId: Long, userId: Long?, status: Submission.Status?, page: Int): Single<PagedList<Submission>> =
        submissionService
            .getSubmissions(getQueryMap(stepId, userId, status, page))
            .map { it.toPagedList(submissionMapper::apply) }

    private fun getQueryMap(stepId: Long, userId: Long?, status: Submission.Status?, page: Int): Map<String, String> =
        mapOfNotNull(
            STEP to stepId.toString(),
            USER to userId?.toString(),
            STATUS to status?.scope,
            PAGE to page.toString()
        )
}