package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.RetryWithDelay
import javax.inject.Inject

class DownloadingSectionWatcher
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val downloadingLessonWatcher: DownloadingLessonWatcher) : DownloadingWatcher {

    companion object {
        private const val RETRY_DELAY = 300
    }

    override fun watch(id: Long): Flowable<Float> =
            Flowable
                    .fromCallable {
                        databaseFacade.getSectionById(id)
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY))
                    .cache()
                    .flatMap {
                        val unitsByIds = databaseFacade.getUnitsByIds(it.units)
                        if (unitsByIds.size != it.units?.size) {
                            throw UnitsAreNotCachedException()
                        }
                        Flowable.fromIterable(unitsByIds)
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY))
                    .cache()
                    .map {
                        it.lesson
                    }
                    .flatMap {
                        downloadingLessonWatcher.watch(it)
                    }


    class UnitsAreNotCachedException : Exception("Units are not in database yet")
}
