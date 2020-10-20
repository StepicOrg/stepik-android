package org.stepik.android.domain.submission.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapNotNullPaged
import ru.nobird.android.core.model.mapToLongArray
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import javax.inject.Inject

class SubmissionInteractor
@Inject
constructor(
    private val submissionRepository: SubmissionRepository,
    private val attemptRepository: AttemptRepository,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository
) {
    fun getSubmissionItems(stepId: Long, status: Submission.Status?, page: Int = 1): Single<PagedList<SubmissionItem.Data>> =
        submissionRepository
            .getSubmissionsForStep(stepId, userPreferences.userId, status, page)
            .flatMap { submissions ->
                val attemptIds = submissions.mapToLongArray(Submission::attempt)

                zip(
                    attemptRepository
                        .getAttempts(*attemptIds),
                    userRepository
                        .getUsers(listOf(userPreferences.userId))
                ) { attempts, users ->
                    mapToSubmissionItems(submissions, attempts, users)
                }
            }

    private fun mapToSubmissionItems(submissions: PagedList<Submission>, attempts: List<Attempt>, users: List<User>): PagedList<SubmissionItem.Data> =
        submissions
            .mapNotNullPaged { submission ->
                val attempt = attempts
                    .find { it.id == submission.attempt }
                    ?: return@mapNotNullPaged null

                val user = users
                    .find { it.id == attempt.user }
                    ?: return@mapNotNullPaged null

                SubmissionItem.Data(submission, attempt, user)
            }
}