package org.stepik.android.data.submission.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Submission

interface SubmissionCacheDataSource {
    fun createSubmission(submission: Submission): Single<Submission>
    fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>>
    fun removeSubmissionsForAttempt(attemptId: Long): Completable
}