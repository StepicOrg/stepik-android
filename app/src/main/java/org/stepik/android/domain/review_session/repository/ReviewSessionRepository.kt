package org.stepik.android.domain.review_session.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_session.model.ReviewSession
import ru.nobird.android.domain.rx.first

interface ReviewSessionRepository {
    fun createReviewSession(submissionId: Long): Single<ReviewSession>

    fun getReviewSession(id: Long, sourceType: DataSourceType = DataSourceType.REMOTE): Single<ReviewSession> =
        getReviewSessions(listOf(id), sourceType).first()

    fun getReviewSessions(ids: List<Long>, sourceType: DataSourceType = DataSourceType.REMOTE): Single<List<ReviewSession>>
}