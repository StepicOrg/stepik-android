package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.step.repository.StepRepository
import javax.inject.Inject

@PersistenceScope
class StepStructureResolverImpl
@Inject
constructor(
    private val stepRepository: StepRepository,
    private val progressRepository: ProgressRepository,
    private val attemptRepository: AttemptRepository
): StepStructureResolver {
    override fun resolveStructure(
        courseId: Long,
        sectionId: Long,
        unitId: Long,
        lessonId: Long,
        vararg stepIds: Long
    ): Observable<Structure> =
        stepRepository
            .getSteps(*stepIds)
            .flatMapObservable { steps ->
                val observables = steps
                    .map { step ->
                        Structure(courseId, sectionId, unitId, lessonId, step.id)
                    }
                    .toObservable()

                val attemptCompletable = Completable
                    .concat(steps.map { resolveStepAttempt(it.id) })

                progressRepository
                    .getProgresses(*steps.getProgresses())
                    .ignoreElement()
                    .andThen(attemptCompletable)
                    .andThen(observables)
            }

    private fun resolveStepAttempt(stepId: Long): Completable =
        attemptRepository
            .getAttemptsForStep(stepId)
            .maybeFirst()
            .switchIfEmpty(attemptRepository.createAttemptForStep(stepId))
            .ignoreElement()
}