package org.stepik.android.data.step.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Step

interface StepCacheDataSource {
    fun getSteps(vararg stepIds: Long): Single<List<Step>>
    fun saveSteps(steps: List<Step>): Completable
}