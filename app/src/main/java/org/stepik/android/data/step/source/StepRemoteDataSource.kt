package org.stepik.android.data.step.source

import io.reactivex.Single
import org.stepik.android.model.Step

interface StepRemoteDataSource {
    fun getSteps(vararg stepIds: Long): Single<List<Step>>
}