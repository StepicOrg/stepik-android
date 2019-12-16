package org.stepik.android.domain.step_source.interactor

import io.reactivex.Single
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.step_source.repository.StepSourceRepository
import org.stepik.android.model.Step
import javax.inject.Inject

class StepSourceInteractor
@Inject
constructor(
    private val stepSourceRepository: StepSourceRepository,
    private val stepRepository: StepRepository,
    private val stepContentResolver: StepContentResolver
) {
    fun changeStepBlockText(step: Step, text: String): Single<StepPersistentWrapper> =
        stepSourceRepository
            .getStepSource(step.id)
            .flatMap { source ->
                stepSourceRepository
                    .saveStepSource(source.copy(block = source.block.copy(text = text)))
            }
            .flatMap {
                stepRepository
                    .getStep(step.id, primarySourceType = DataSourceType.REMOTE)
                    .toSingle()
            }
            .flatMap(stepContentResolver::resolvePersistentContent)

    fun fetchStep(step: Step): Single<StepPersistentWrapper> =
        stepRepository
            .getStep(step.id, primarySourceType = DataSourceType.REMOTE)
            .toSingle()
            .flatMap(stepContentResolver::resolvePersistentContent)
}