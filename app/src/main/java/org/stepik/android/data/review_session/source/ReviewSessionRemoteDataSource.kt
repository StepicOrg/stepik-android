package org.stepik.android.data.review_session.source

import io.reactivex.Single
import org.stepik.android.domain.review_session.model.ReviewSessionData

interface ReviewSessionRemoteDataSource {
    fun createReviewSession(submissionId: Long): Single<ReviewSessionData>
    fun getReviewSessions(ids: List<Long>): Single<List<ReviewSessionData>>
}