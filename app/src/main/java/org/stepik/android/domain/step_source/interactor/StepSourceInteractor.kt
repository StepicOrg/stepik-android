package org.stepik.android.domain.step_source.interactor

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.domain.step_source.repository.StepSourceRepository
import org.stepik.android.model.Step
import javax.inject.Inject

class StepSourceInteractor
@Inject
constructor(
    private val stepSourceRepository: StepSourceRepository,
    private val stepRepository: StepRepository
) {
    fun changeStepBlockText(step: Step, text: String): Single<Step> =
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
}