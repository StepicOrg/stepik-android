package org.stepik.android.remote.step

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.model.Step
import org.stepik.android.remote.step.model.StepResponse
import javax.inject.Inject

class StepRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : StepRemoteDataSource {
    override fun getSteps(vararg stepIds: Long): Single<List<Step>> =
        api.getSteps(stepIds)
            .map(StepResponse::steps)
}