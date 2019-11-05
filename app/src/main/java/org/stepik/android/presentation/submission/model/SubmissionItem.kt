package org.stepik.android.presentation.submission.model

import org.stepik.android.model.Submission
import ru.nobird.android.core.model.Identifiable

sealed class SubmissionItem {
    data class Data(
        val submission: Submission
    ) : SubmissionItem(), Identifiable<Long> {
        override val id: Long =
            submission.id
    }
}