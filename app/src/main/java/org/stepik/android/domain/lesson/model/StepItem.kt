package org.stepik.android.domain.lesson.model

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.model.Assignment
import org.stepik.android.model.Progress

data class StepItem(
    val stepWrapper: StepPersistentWrapper,
    val stepProgress: Progress?,

    val assignment: Assignment?,
    val assignmentProgress: Progress?,

    val reviewSession: ReviewSession?
)