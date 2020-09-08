package org.stepik.android.data.review_session.source

import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Single

interface ReviewSessionRemoteDataSource {
    fun createReviewSession(submissionId: Long): Single<ReviewSession>
    fun getReviewSession(id: Long): Single<ReviewSession>
}