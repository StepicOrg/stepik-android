package org.stepik.android.remote.step_source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.step_source.source.StepSourceRemoteDataSource
import org.stepik.android.model.StepSource
import org.stepik.android.remote.step_source.model.StepSourceRequest
import org.stepik.android.remote.step_source.model.StepSourceResponse
import javax.inject.Inject

class StepSourceRemoteDataSourceImpl
@Inject
constructor(
    private val stepicRestLoggedService: StepicRestLoggedService
) : StepSourceRemoteDataSource {
    private val responseMapper =
        Function<StepSourceResponse, StepSource> { it.stepSources.first() }

    override fun getStepSource(stepId: Long): Single<StepSource> =
        stepicRestLoggedService
            .getStepSources(longArrayOf(stepId))
            .map(responseMapper)

    override fun saveStepSource(stepSource: StepSource): Single<StepSource> =
        stepicRestLoggedService
            .saveStepSource(stepSource.id, StepSourceRequest(stepSource))
            .map(responseMapper)
}