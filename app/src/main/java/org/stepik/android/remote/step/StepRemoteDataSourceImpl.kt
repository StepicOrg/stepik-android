package org.stepik.android.remote.step

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.model.Step
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.step.model.StepResponse
import org.stepik.android.remote.step.service.StepService
import javax.inject.Inject

class StepRemoteDataSourceImpl
@Inject
constructor(
    private val stepService: StepService
) : StepRemoteDataSource {
    private val stepResponseMapper =
        Function<StepResponse, List<Step>>(StepResponse::steps)

    override fun getSteps(vararg stepIds: Long): Single<List<Step>> =
        stepIds
            .chunkedSingleMap { ids ->
                stepService.getSteps(ids)
                    .map(stepResponseMapper)
            }

    override fun getStepByLessonId(lessonId: Long): Single<Step> =
        stepService.getStepsByLessonId(lessonId).map { it.steps.firstOrNull() }
}