package org.stepik.android.domain.view_assignment.interactor

import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.util.AppConstants
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.model.Assignment
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.model.ViewAssignment
import org.stepik.android.view.injection.view_assignment.ViewAssignmentBus
import javax.inject.Inject

class ViewAssignmentReportInteractor
@Inject
constructor(
    private val viewAssignmentRepository: ViewAssignmentRepository,
    private val localProgressInteractor: LocalProgressInteractor,

    private val lastStepRepository: LastStepRepository,
    private val progressRepository: ProgressRepository,

    private val progressesPublisher: PublishSubject<Progress>,

    @ViewAssignmentBus
    private val viewAssignmentObserver: BehaviorSubject<kotlin.Unit>
) {
    fun updatePassedStep(step: Step, assignment: Assignment?): Completable =
        updateLocalStepProgress(step, assignment)
            .andThen(localProgressInteractor.updateStepsProgress(listOf(step)))

    fun reportViewAssignment(step: Step, assignment: Assignment?, unit: Unit?, course: Course?): Completable =
        updateLocalLastStep(step, unit, course)
            .andThen(updateLocalStepProgress(step, assignment))
            .andThen(postViewAssignmentAndUpdateStepProgress(step, ViewAssignment(assignment?.id, step.id)))

    private fun updateLocalLastStep(step: Step, unit: Unit?, course: Course?): Completable {
        val lastStepId = course?.lastStepId
        return if (unit != null && lastStepId != null) {
            lastStepRepository
                .saveLastStep(LastStep(lastStepId, unit.id, unit.lesson, step.id))
        } else {
            Completable
                .complete()
        }
    }

    private fun updateLocalStepProgress(step: Step, assignment: Assignment?): Completable =
        if (isStepPassedAfterView(step)) {
            val progresses =
                listOfNotNull(
                    Progress(id = step.progress, isPassed = true, nSteps = 1, nStepsPassed = 1),
                    assignment?.progress?.let { Progress(id = it, isPassed = true, nSteps = 1, nStepsPassed = 1) }
                )

            progressRepository
                .saveProgresses(progresses)
                .doOnComplete {
                    progresses.forEach(progressesPublisher::onNext)
                }
        } else {
            Completable
                .complete()
        }

    /**
     * Return true if it's enough to view [step] to pass it
     */
    private fun isStepPassedAfterView(step: Step): Boolean =
        when (step.block?.name) {
            AppConstants.TYPE_TEXT,
            AppConstants.TYPE_VIDEO ->
                true

            else ->
                false
        }

    /**
     * Tries to post view assignment to remote server and update remote progress or add view assignment to local queue
     */
    private fun postViewAssignmentAndUpdateStepProgress(step: Step, viewAssignment: ViewAssignment): Completable =
        viewAssignmentRepository
            .createViewAssignment(viewAssignment, dataSourceType = DataSourceType.REMOTE)
            .andThen(localProgressInteractor.updateStepsProgress(listOf(step))) // propagate progress updates only if successfully posted view assignments
            .onErrorResumeNext {
                viewAssignmentRepository
                    .createViewAssignment(viewAssignment, dataSourceType = DataSourceType.CACHE) // add view assignment to local queue
                    .doOnComplete {
                        viewAssignmentObserver.onNext(kotlin.Unit)
                    }
            }
}