package org.stepik.android.domain.lesson.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step.repository.StepRepository
import javax.inject.Inject

class LessonContentInteractor
@Inject
constructor(
    private val stepRepository: StepRepository,
    private val stepContentResolver: StepContentResolver
) {
    fun getSteps(vararg stepIds: Long): Single<List<StepPersistentWrapper>> =
        stepRepository
            .getSteps(*stepIds)
            .flatMapObservable { it.toObservable() }
            .flatMap(stepContentResolver::resolvePersistentContent)
            .toList()
}