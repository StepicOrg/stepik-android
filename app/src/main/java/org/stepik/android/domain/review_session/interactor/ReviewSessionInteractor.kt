package org.stepik.android.domain.review_session.interactor

import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import org.stepik.android.domain.review_session.model.ReviewSession
import javax.inject.Inject

class ReviewSessionInteractor
@Inject
constructor(
    private val reviewSessionRepository: ReviewSessionRepository
) {

}