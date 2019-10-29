package org.stepik.android.domain.step_source.repository

import io.reactivex.Single
import org.stepik.android.model.StepSource

interface StepSourceRepository {
    fun getStepSource(stepId: Long): Single<StepSource>
    fun saveStepSource(stepSource: StepSource): Single<StepSource>
}