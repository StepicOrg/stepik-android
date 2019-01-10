package org.stepik.android.cache.last_step

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.last_step.source.LastStepCacheDataSource
import org.stepik.android.domain.last_step.model.LastStep
import javax.inject.Inject

class LastStepCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : LastStepCacheDataSource {
    override fun getLastStep(id: String): Maybe<LastStep> =
        Maybe.create { emitter ->
            databaseFacade.getLocalLastStepById(id)
                ?.let(emitter::onSuccess)
                ?: emitter.onComplete()
        }

    override fun saveLastStep(lastStep: LastStep): Completable =
        Completable.fromAction {
            databaseFacade.updateLastStep(lastStep)
        }
}