package org.stepik.android.remote.step

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.model.Step
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.step.model.StepResponse
import javax.inject.Inject

class StepRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : StepRemoteDataSource {
    private val stepResponseMapper =
        Function<StepResponse, List<Step>>(StepResponse::steps)

    override fun getSteps(vararg stepIds: Long): Single<List<Step>> =
        stepIds
            .chunkedSingleMap { ids ->
                api.getSteps(ids)
                    .map(stepResponseMapper)
            }
}