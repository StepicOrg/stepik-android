package org.stepik.android.data.last_step.source

import io.reactivex.Maybe
import org.stepik.android.domain.last_step.model.LastStep

interface LastStepRemoteDataSource {
    fun getLastStep(id: String): Maybe<LastStep>
}