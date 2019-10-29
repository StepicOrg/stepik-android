package org.stepik.android.data.step_source.source

import io.reactivex.Single
import org.stepik.android.model.StepSource

interface StepSourceRemoteDataSource {
    fun getStepSource(stepId: Long): Single<StepSource>
    fun saveStepSource(stepSource: StepSource): Single<StepSource>
}