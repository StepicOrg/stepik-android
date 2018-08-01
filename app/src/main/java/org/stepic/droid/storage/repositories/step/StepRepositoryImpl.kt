package org.stepic.droid.storage.repositories.step

import org.stepik.android.model.Step
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Step> {

    override fun getObjects(keys: LongArray): Iterable<Step> {
        var steps = databaseFacade.getStepsById(keys)
        if (steps.size != keys.size) {
            steps =
                    try {
                        api.getSteps(keys).execute()?.body()?.steps?.also {
                            it.forEach(databaseFacade::addStep)
                        } ?: emptyList()
                    } catch (exception: Exception) {
                        emptyList()
                    }
        }
        steps = steps.sortedBy { it.position }
        return steps
    }

    override fun getObject(key: Long): Step? {
        var step = databaseFacade.getStepById(key)
        if (step == null) {
            step =
                    try {
                        api.getSteps(longArrayOf(key)).execute()
                                ?.body()
                                ?.steps
                                ?.firstOrNull()
                                ?.also(databaseFacade::addStep)
                    } catch (exception: Exception) {
                        null
                    }
        }
        return step
    }

}


