package org.stepik.android.domain.last_step.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.last_step.model.LastStep

interface LastStepRepository {
    fun getLastStep(id: String): Maybe<LastStep>

    /**
     * Saves [lastStep] locally
     */
    fun saveLastStep(lastStep: LastStep): Completable
}