package org.stepik.android.cache.review_session

import org.stepik.android.cache.review_session.dao.ReviewSessionDao
import org.stepik.android.data.review_session.source.ReviewSessionCacheDataSource
import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.attempt.structure.DbStructureAttempt
import org.stepik.android.cache.submission.structure.DbStructureSubmission
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class ReviewSessionCacheDataSourceImpl
@Inject
constructor(
    private val reviewSessionDao: ReviewSessionDao,
    private val submissionDao: IDao<Submission>,
    private val attemptDao: IDao<Attempt>
) : ReviewSessionCacheDataSource {
    override fun getReviewSessions(ids: List<Long>): Single<List<ReviewSessionData>> =
        Single
            .fromCallable {
                reviewSessionDao
                    .getReviewSessions(ids)
                    .map { session ->
                        val submission =
                            if (session.submission != 0L) {
                                requireNotNull(submissionDao.get(DbStructureSubmission.Columns.ID, session.submission.toString()))
                            } else {
                                null
                            }
                        val attempt =
                            if (submission != null) {
                                requireNotNull(attemptDao.get(DbStructureAttempt.Columns.ID, submission.attempt.toString()))
                            } else {
                                null
                            }

                        ReviewSessionData(session, submission, attempt)
                    }
            }

    override fun saveReviewSessions(items: List<ReviewSessionData>): Completable =
        Completable
            .fromCallable {
                items.forEach {
                    reviewSessionDao.saveReviewSession(it.session)
                    it.submission?.let { submission -> submissionDao.insertOrUpdate(submission) }
                    it.attempt?.let { attempt -> attemptDao.insertOrUpdate(attempt) }
                }
            }
}