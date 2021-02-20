package org.stepik.android.domain.submission.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.mapNotNullPaged
import ru.nobird.android.core.model.mapToLongArray
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
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
    fun getSubmissionItems(stepId: Long, isTeacher: Boolean, submissionsFilterQuery: SubmissionsFilterQuery): Single<PagedList<SubmissionItem.Data>> =
        submissionRepository
            .getSubmissionsForStep(
                stepId,
                submissionsFilterQuery.copy(user = if (isTeacher) null else userPreferences.userId)
            )
            .flatMap { submissions ->
                resolveSubmissionItems(isTeacher, submissions)
            }

    private fun resolveSubmissionItems(isTeacher: Boolean, submissions: PagedList<Submission>): Single<PagedList<SubmissionItem.Data>> {
        val attemptIds = submissions.mapToLongArray(Submission::attempt)

        return if (isTeacher) {
            attemptRepository
                .getAttempts(*attemptIds)
                .flatMap { attempts ->
                    resolveAttemptsAndUsersForTeacher(submissions, attempts)
                }
        } else {
            zip(
                attemptRepository
                    .getAttempts(*attemptIds),
                userRepository
                    .getUsers(listOf(userPreferences.userId))
            ) { attempts, users ->
                mapToSubmissionItems(submissions, attempts, users)
            }
        }
    }

    private fun resolveAttemptsAndUsersForTeacher(submissions: PagedList<Submission>, attempts: List<Attempt>): Single<PagedList<SubmissionItem.Data>> =
        userRepository
            .getUsers(attempts.map(Attempt::user))
            .flatMap { users ->
                Single.just(mapToSubmissionItems(submissions, attempts, users))
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