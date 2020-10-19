package org.stepik.android.domain.lesson.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.domain.lesson.model.StepItem
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.model.Assignment
import org.stepik.android.model.Progress
import org.stepik.android.model.Unit
import javax.inject.Inject

class LessonContentInteractor
@Inject
constructor(
    private val assignmentRepository: AssignmentRepository,
    private val stepRepository: StepRepository,
    private val progressRepository: ProgressRepository,
    private val stepContentResolver: StepContentResolver,
    private val reviewSessionRepository: ReviewSessionRepository
) {
    fun getStepItems(unit: Unit?, vararg stepIds: Long): Single<List<StepItem>> =
        zip(
            getAssignments(unit),
            getSteps(*stepIds)
        )
            .flatMap { (assignments, steps) ->
                val progressIds = assignments.getProgresses() + steps.getProgresses()
                val reviewSessionsIds = steps.mapNotNull { it.step.session }

                zip(
                    progressRepository.getProgresses(progressIds),
                    reviewSessionRepository.getReviewSessions(reviewSessionsIds)
                ) { progresses, reviewSessions ->
                    packStepItems(assignments, steps, progresses, reviewSessions)
                }
            }

    private fun getSteps(vararg stepIds: Long): Single<List<StepPersistentWrapper>> =
        stepRepository
            .getSteps(*stepIds)
            .flatMapObservable { it.toObservable() }
            .flatMapSingle(stepContentResolver::resolvePersistentContent)
            .toList()

    private fun getAssignments(unit: Unit?): Single<List<Assignment>> =
        assignmentRepository
            .getAssignments(*unit?.assignments ?: emptyList())

    private fun packStepItems(assignments: List<Assignment>, steps: List<StepPersistentWrapper>, progresses: List<Progress>, reviewSessions: List<ReviewSessionData>): List<StepItem> =
        steps
            .map { stepWrapper ->
                val assignment = assignments
                    .find { it.step == stepWrapper.step.id }

                val reviewSession = reviewSessions
                    .find { it.id == stepWrapper.step.session }

                StepItem(
                    stepWrapper = stepWrapper,
                    stepProgress = progresses.find { it.id == stepWrapper.progress },

                    assignment = assignment,
                    assignmentProgress = progresses.find { it.id == assignment?.progress },

                    reviewSession = reviewSession?.session
                )
            }
}