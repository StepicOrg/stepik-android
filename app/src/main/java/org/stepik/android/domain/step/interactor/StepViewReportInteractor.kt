package org.stepik.android.domain.step.interactor

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.domain.progress.interactor.LocalProgressInteractor
import org.stepik.android.domain.view_assignment.repository.ViewAssignmentRepository
import org.stepik.android.model.Assignment
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.model.ViewAssignment
import javax.inject.Inject

class StepViewReportInteractor
@Inject
constructor(
    private val viewAssignmentRepository: ViewAssignmentRepository,
    private val localProgressInteractor: LocalProgressInteractor,

    private val lastStepRepository: LastStepRepository,

    private val progressesPublisher: PublishSubject<Progress>
) {
    fun reportStepView(step: Step, assignment: Assignment?, unit: Unit?, course: Course?): Completable =
        updateLocalLastStep(step, unit, course)
            .doOnComplete {
                progressesPublisher.onNext(Progress(id = step.progress, isPassed = true, nSteps = 1, nStepsPassed = 1))
                progressesPublisher.onNext(Progress(id = assignment?.progress, isPassed = true, nSteps = 1, nStepsPassed = 1))
            }
            .andThen(viewAssignmentRepository.createViewAssignment(ViewAssignment(assignment?.id, step.id)))
            .andThen(localProgressInteractor.updateStepsProgress(listOf(step)))

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
}