package org.stepik.android.domain.review_session.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_session.model.ReviewSessionData
import ru.nobird.android.domain.rx.first

interface ReviewSessionRepository {
    fun createReviewSession(submissionId: Long): Single<ReviewSessionData>

    fun getReviewSession(id: Long, sourceType: DataSourceType = DataSourceType.REMOTE): Single<ReviewSessionData> =
        getReviewSessions(listOf(id), sourceType).first()

    fun getReviewSessions(ids: List<Long>, sourceType: DataSourceType = DataSourceType.REMOTE): Single<List<ReviewSessionData>>
}