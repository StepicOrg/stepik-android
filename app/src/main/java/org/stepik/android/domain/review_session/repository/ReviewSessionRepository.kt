package org.stepik.android.domain.review_session.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_session.model.ReviewSession

interface ReviewSessionRepository {
    fun createReviewSession(submissionId: Long): Single<ReviewSession>

    fun getReviewSession(
        id: Long,
        primarySourceType: DataSourceType = DataSourceType.REMOTE
    ): Single<ReviewSession>
}