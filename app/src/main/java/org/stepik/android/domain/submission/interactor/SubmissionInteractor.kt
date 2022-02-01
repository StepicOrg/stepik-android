package org.stepik.android.domain.submission.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.preferences.UserPreferences
import ru.nobird.app.core.model.PagedList
import ru.nobird.app.core.model.mapToLongArray
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import ru.nobird.app.core.model.transform
import javax.inject.Inject

class SubmissionInteractor
@Inject
constructor(
    private val submissionRepository: SubmissionRepository,
    private val attemptRepository: AttemptRepository,
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    private val reviewSessionRepository: ReviewSessionRepository
) {
    fun getSubmissionItems(
        stepId: Long,
        isTeacher: Boolean,
        submissionsFilterQuery: SubmissionsFilterQuery,
        reviewInstruction: ReviewInstruction? = null,
        page: Int = 1
    ): Single<PagedList<SubmissionItem.Data>> =
        submissionRepository
            .getSubmissionsForStep(
                stepId,
                submissionsFilterQuery.copy(user = if (isTeacher) null else userPreferences.userId),
                page
            )
            .flatMap { submissions -> resolveSubmissionItems(submissions) }
            .flatMap { submissionItems ->
                if (reviewInstruction != null) {
                    resolveSubmissionItemsWithReviewSessionData(submissionItems, reviewInstruction.id)
                } else {
                    Single.just(submissionItems)
                }
            }

    private fun resolveSubmissionItemsWithReviewSessionData(submissionItems: PagedList<SubmissionItem.Data>, instructionId: Long): Single<PagedList<SubmissionItem.Data>> {
        val sessions = submissionItems.mapNotNull { it.submission.session }
        val users = submissionItems.filter { it.submission.session == null }.map { it.user }
        return zip(
            fetchReviewSessionsBySession(sessions),
            fetchReviewSessionsByUserAndInstruction(instructionId, users)
        ) { reviewSessionsBySession, reviewSessionsByUsers ->
            val reviewSessions = reviewSessionsBySession + reviewSessionsByUsers
            submissionItems.transform {
                mapNotNull { submissionItem ->
                    val reviewSessionData = reviewSessions
                        .find { it.attempt?.user == submissionItem.user.id }

                    submissionItem.copy(reviewSessionData = reviewSessionData)
                }
            }
        }
    }

    private fun resolveSubmissionItems(submissions: PagedList<Submission>): Single<PagedList<SubmissionItem.Data>> {
        val attemptIds = submissions.mapToLongArray(Submission::attempt)

        return attemptRepository
            .getAttempts(*attemptIds)
            .flatMap { attempts ->
                resolveAttemptsAndUsers(submissions, attempts)
            }
    }

    private fun resolveAttemptsAndUsers(submissions: PagedList<Submission>, attempts: List<Attempt>): Single<PagedList<SubmissionItem.Data>> =
        userRepository
            .getUsers(attempts.map(Attempt::user))
            .map { users ->
                mapToSubmissionItems(submissions, attempts, users)
            }

    private fun mapToSubmissionItems(submissions: PagedList<Submission>, attempts: List<Attempt>, users: List<User>): PagedList<SubmissionItem.Data> =
        submissions
            .transform {
                mapNotNull { submission ->
                    val attempt = attempts
                        .find { it.id == submission.attempt }
                        ?: return@mapNotNull null

                    val user = users
                        .find { it.id == attempt.user }
                        ?: return@mapNotNull null

                    SubmissionItem.Data(submission, attempt, user)
                }
            }

    private fun fetchReviewSessionsBySession(sessions: List<Long>): Single<List<ReviewSessionData>> =
        reviewSessionRepository.getReviewSessions(sessions)

    private fun fetchReviewSessionsByUserAndInstruction(instructionId: Long, users: List<User>): Single<List<ReviewSessionData>> =
        users
            .toObservable()
            .flatMapMaybe { reviewSessionRepository.getReviewSession(instructionId, it.id) }
            .reduce(emptyList<ReviewSessionData>()) { a, b -> a + b }
}