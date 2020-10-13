package org.stepik.android.domain.review_session.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import ru.nobird.android.core.model.Identifiable

data class ReviewSessionData(
    val session: ReviewSession,
    val submission: Submission,
    val attempt: Attempt
) : Identifiable<Long> {
    override val id: Long =
        session.id
}