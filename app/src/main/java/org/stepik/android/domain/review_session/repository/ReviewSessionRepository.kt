package org.stepik.android.domain.review_session.repository

import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType

interface ReviewSessionRepository {
    fun createReviewSession(submissionId: Long): Single<ReviewSession>

    fun getReviewSession(
        id: Long,
        primarySourceType: DataSourceType = DataSourceType.REMOTE
    ): Single<ReviewSession>
}