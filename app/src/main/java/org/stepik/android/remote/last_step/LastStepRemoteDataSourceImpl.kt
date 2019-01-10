package org.stepik.android.remote.last_step

import io.reactivex.Maybe
import org.stepic.droid.web.Api
import org.stepik.android.data.last_step.source.LastStepRemoteDataSource
import org.stepik.android.domain.last_step.model.LastStep
import javax.inject.Inject

class LastStepRemoteDataSourceImpl
@Inject
constructor(
   private val api: Api
) : LastStepRemoteDataSource {
    override fun getLastStep(id: String): Maybe<LastStep> =
        Maybe.create { emitter ->
            api.getLastStepResponse(id)
                .execute()
                .body()
                ?.lastSteps
                ?.firstOrNull()
                ?.let {
                    it.unit ?: return@let null
                    it.lesson ?: return@let null
                    it.step ?: return@let null
                    LastStep(id = it.id, unit = it.unit, lesson = it.lesson, step = it.step)
                }
                ?.let(emitter::onSuccess)
                ?: emitter.onComplete()
        }
}