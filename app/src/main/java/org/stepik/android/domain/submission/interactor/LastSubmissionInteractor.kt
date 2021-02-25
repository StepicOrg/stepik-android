package org.stepik.android.domain.submission.interactor

import io.reactivex.Maybe
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import javax.inject.Inject

class LastSubmissionInteractor
@Inject
constructor(
    private val submissionRepository: SubmissionRepository,
    private val userPreferences: UserPreferences
) {
    fun getLastSubmission(stepId: Long): Maybe<Submission> =
        submissionRepository
            .getSubmissionsForStep(stepId, SubmissionsFilterQuery(user = userPreferences.userId))
            .map { it as List<Submission> }
            .maybeFirst()
}