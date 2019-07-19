package org.stepik.android.cache.submission

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.submission.structure.DbStructureSubmission
import org.stepik.android.data.submission.source.SubmissionCacheDataSource
import org.stepik.android.model.Submission
import javax.inject.Inject

class SubmissionCacheDataSourceImpl
@Inject
constructor(
    private val submissionDao: IDao<Submission>
) : SubmissionCacheDataSource {
    override fun createSubmission(submission: Submission): Single<Submission> =
        Single.fromCallable {
            submissionDao.insertOrReplace(submission)
            submission
        }

    override fun getSubmissionsForAttempt(attemptId: Long): Single<List<Submission>> =
        Single.fromCallable {
            submissionDao.getAll(DbStructureSubmission.Columns.ATTEMPT_ID, attemptId.toString())
        }

    override fun removeSubmissionsForAttempt(attemptId: Long): Completable =
        Completable.fromAction {
            submissionDao.remove(DbStructureSubmission.Columns.ATTEMPT_ID, attemptId.toString())
        }
}