package org.stepik.android.domain.submission.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import ru.nobird.android.core.model.Identifiable

sealed class SubmissionItem {
    data class Data(
        val submission: Submission,
        val attempt: Attempt
    ) : SubmissionItem(), Identifiable<Long> {
        override val id: Long =
            submission.id
    }

    object Placeholder : SubmissionItem()
}