package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.RetryWithDelay
import javax.inject.Inject

class DownloadingSectionWatcher
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val stepProgressPublisher: StepProgressPublisher) : DownloadingWatcher {

    companion object {
        private const val RETRY_DELAY = 300
    }

    override fun watch(id: Long): Flowable<Float> =
            Flowable
                    .fromCallable {
                        databaseFacade.getSectionById(id)
                    }
                    .flatMap {
                        Flowable.fromIterable(databaseFacade.getUnitsByIds(it.units))
                    }
                    .cache()
                    .map {
                        databaseFacade.getLessonOfUnit(it) //it can be null
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //retry if lessons are empty in database
                    .map {
                        it.steps
                    }
                    .map {
                        it.toMutableList()
                    }
                    .reduce { accumulator: MutableList<Long>, item: MutableList<Long> ->
                        accumulator.addAll(item)
                        accumulator
                    }
                    .map {
                        it.toSet()
                    }
                    .cache()
                    .toFlowable()
                    .flatMap {
                        stepProgressPublisher.subscribe(it)
                    }
}
