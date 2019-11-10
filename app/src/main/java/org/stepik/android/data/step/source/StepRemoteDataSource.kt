package org.stepik.android.data.step.source

import io.reactivex.Single
import org.stepik.android.model.Step
import org.stepik.android.remote.step.model.StepResponse

interface StepRemoteDataSource {
    fun getSteps(vararg stepIds: Long): Single<List<Step>>

    fun getStepByLessonId(lessonId: Long): Single<StepResponse>
}