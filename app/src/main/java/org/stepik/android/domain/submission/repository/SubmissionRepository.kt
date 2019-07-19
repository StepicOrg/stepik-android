package org.stepik.android.domain.submission.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Submission

interface SubmissionRepository {
    fun createSubmission(submission: Submission, dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<Submission>
    fun getSubmissionsForAttempt(attemptId: Long, dataSourceType: DataSourceType = DataSourceType.REMOTE): Single<List<Submission>>
    fun getSubmissionsForStep(stepId: Long): Single<List<Submission>>

    fun removeSubmissionsForAttempt(attemptId: Long): Completable
}