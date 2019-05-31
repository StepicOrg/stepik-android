package org.stepik.android.cache.step

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.step.source.StepCacheDataSource
import org.stepik.android.model.Step
import javax.inject.Inject

class StepCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : StepCacheDataSource {
    override fun getSteps(vararg stepIds: Long): Single<List<Step>> =
        Single.fromCallable {
            databaseFacade.getStepsById(stepIds)
        }

    override fun saveSteps(steps: List<Step>): Completable =
        Completable.fromAction {
            databaseFacade.addSteps(steps)
        }
}