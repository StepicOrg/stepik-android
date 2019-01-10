package org.stepik.android.data.last_step.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.last_step.model.LastStep

interface LastStepCacheDataSource {
    fun getLastStep(id: String): Maybe<LastStep>
    fun saveLastStep(lastStep: LastStep): Completable
}