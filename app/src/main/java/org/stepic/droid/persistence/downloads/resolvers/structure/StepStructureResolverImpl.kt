package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.Structure
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.step.repository.StepRepository
import javax.inject.Inject

@PersistenceScope
class StepStructureResolverImpl
@Inject
constructor(
    private val stepRepository: StepRepository,
    private val progressRepository: ProgressRepository
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

                progressRepository
                    .getProgresses(*steps.getProgresses())
                    .ignoreElement()
                    .andThen(observables)
            }
}