package org.stepik.android.domain.submission.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapNotNullPaged
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.domain.submission.model.SubmissionItem
import javax.inject.Inject

class SubmissionInteractor
@Inject
constructor(
    private val submissionRepository: SubmissionRepository,
    private val attemptRepository: AttemptRepository,
    private val userPreferences: UserPreferences
) {
    fun getSubmissionItems(stepId: Long, page: Int = 1): Single<PagedList<SubmissionItem.Data>> =
        submissionRepository
            .getSubmissionsForStep(stepId, userPreferences.userId, page)
            .flatMap { submissions ->
                val attemptIds = submissions.mapToLongArray(Submission::attempt)
                attemptRepository
                    .getAttempts(*attemptIds)
                    .map { attempts ->
                        mapToSubmissionItems(submissions, attempts)
                    }
            }

    private fun mapToSubmissionItems(submissions: PagedList<Submission>, attempts: List<Attempt>): PagedList<SubmissionItem.Data> =
        submissions
            .mapNotNullPaged { submission ->
                val attempt = attempts
                    .find { it.id == submission.attempt }
                    ?: return@mapNotNullPaged null

                SubmissionItem.Data(submission, attempt)
            }
}