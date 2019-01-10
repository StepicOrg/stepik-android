package org.stepik.android.domain.last_step.repository

import io.reactivex.Maybe
import org.stepik.android.domain.last_step.model.LastStep

interface LastStepRepository {
    fun getLastStep(id: String): Maybe<LastStep>
}