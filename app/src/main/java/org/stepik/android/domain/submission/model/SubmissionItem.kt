package org.stepik.android.domain.submission.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import ru.nobird.android.core.model.Identifiable

class SubmissionItem(
    val submission: Submission,
    val attempt: Attempt
) : Identifiable<Long> {
    override val id: Long =
        submission.id
}