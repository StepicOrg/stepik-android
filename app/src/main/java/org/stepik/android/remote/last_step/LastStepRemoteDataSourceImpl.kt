package org.stepik.android.remote.last_step

import io.reactivex.Maybe
import org.stepik.android.data.last_step.source.LastStepRemoteDataSource
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.remote.last_step.service.LastStepService
import javax.inject.Inject

class LastStepRemoteDataSourceImpl
@Inject
constructor(
    private val lastStepService: LastStepService
) : LastStepRemoteDataSource {
    override fun getLastStep(id: String): Maybe<LastStep> =
        lastStepService
            .getLastStep(id)
            .flatMapMaybe { response ->
                Maybe.fromCallable {
                    response
                        .lastSteps
                        .firstOrNull()
                        ?.let {
                            it.unit ?: return@let null
                            it.lesson ?: return@let null
                            it.step ?: return@let null
                            LastStep(id = it.id, unit = it.unit, lesson = it.lesson, step = it.step)
                        }
                }
            }
}