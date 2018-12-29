package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Step
import javax.inject.Inject

@PersistenceScope
class StepStructureResolverImpl
@Inject
constructor(
        private val stepRepository: Repository<Step>,
        private val progressRepository: ProgressRepository
): StepStructureResolver {
    override fun resolveStructure(
            courseId: Long,
            sectionId: Long,
            unitId: Long,
            lessonId: Long,
            vararg stepIds: Long
    ): Observable<Structure> = Observable
            .just(stepIds)
            .map(stepRepository::getObjects)
            .flatMap { steps ->
                val observables = steps
                    .map { step ->
                        Structure(courseId, sectionId, unitId, lessonId, step.id)
                    }
                    .toObservable()

                progressRepository
                    .getProgresses(*steps.getProgresses())
                    .toCompletable()
                    .andThen(observables)
            }
}