package org.stepik.android.domain.step.interactor

import io.reactivex.Observable
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.concat
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.view.injection.step.StepDiscussionBus
import javax.inject.Inject

class StepInteractor
@Inject
constructor(
    @StepDiscussionBus
    private val stepDiscussionObservable: Observable<Long>,
    private val stepRepository: StepRepository,
    private val stepContentResolver: StepContentResolver
) {
    fun getStepUpdates(stepId: Long, shouldSkipFirstValue: Boolean = false, cacheResult: Boolean = true): Observable<StepPersistentWrapper> =
        Observable
            .just(stepId)
            .concat(stepDiscussionObservable)
            .skip(if (shouldSkipFirstValue) 1 else 0)
            .filter { it == stepId }
            .flatMapMaybe { stepRepository.getStep(stepId, DataSourceType.REMOTE, cacheResult) }
            .flatMap(stepContentResolver::resolvePersistentContent)
}