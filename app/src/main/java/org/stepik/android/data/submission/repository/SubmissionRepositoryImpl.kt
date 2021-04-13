package org.stepik.android.data.submission.repository

import io.reactivex.Completable
import io.reactivex.Single
import ru.nobird.android.core.model.PagedList
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepik.android.data.submission.source.SubmissionCacheDataSource
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import javax.inject.Inject

class SubmissionRepositoryImpl
@Inject
constructor(
    private val submissionRemoteDataSource: SubmissionRemoteDataSource,
    private val submissionCacheDataSource: SubmissionCacheDataSource
) : SubmissionRepository {
    override fun createSubmission(submission: Submission, dataSourceType: DataSourceType): Single<Submission> =
        when (dataSourceType) {
            DataSourceType.CACHE ->
                submissionCacheDataSource.createSubmission(submission)

            DataSourceType.REMOTE ->
                submissionRemoteDataSource
                    .createSubmission(submission)
                    .flatMap(submissionCacheDataSource::createSubmission)
        }

    override fun getSubmissionsForAttempt(attemptId: Long, dataSourceType: DataSourceType): Single<List<Submission>> =
        when (dataSourceType) {
            DataSourceType.CACHE ->
                submissionCacheDataSource.getSubmissionsForAttempt(attemptId)

            DataSourceType.REMOTE ->
                submissionRemoteDataSource
                    .getSubmissionsForAttempt(attemptId)
                    .doCompletableOnSuccess { submissions ->
                        submissions
                            .firstOrNull()
                            ?.let(submissionCacheDataSource::createSubmission)
                            ?.ignoreElement()
                            ?: Completable.complete()
                    }
        }

    override fun getSubmissionsForStep(stepId: Long, submissionsFilterQuery: SubmissionsFilterQuery, page: Int): Single<PagedList<Submission>> =
        submissionRemoteDataSource.getSubmissionsForStep(stepId, submissionsFilterQuery, page)

    override fun removeSubmissionsForAttempt(attemptId: Long): Completable =
        submissionCacheDataSource.removeSubmissionsForAttempt(attemptId)
}