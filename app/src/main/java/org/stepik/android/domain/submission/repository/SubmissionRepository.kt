package org.stepik.android.domain.submission.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Submission

interface SubmissionRepository {
    fun createSubmission(submission: Submission, dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<Submission>
    fun getSubmissionsForAttempt(attemptId: Long, dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<List<Submission>>

    /**
     * If [userId] not specified all available submissions will be returned
     */
    fun getSubmissionsForStep(stepId: Long, userId: Long? = null, page: Int = 1): Single<PagedList<Submission>>

    fun removeSubmissionsForAttempt(attemptId: Long): Completable
}