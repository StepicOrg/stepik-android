package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.RetryWithDelay
import javax.inject.Inject

class ProgressLessonWatcher
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val stepProgressPublisher: StepProgressPublisher
) : ProgressWatcher {

    companion object {
        private const val RETRY_DELAY: Int = 300
    }

    override fun watch(id: Long): Flowable<Float> =
            Flowable
                    .fromCallable {
                        //it can be null
                        databaseFacade.getLessonById(id) ?: throw LessonIsNotCachedException()

                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //retry if lessons are empty in database
                    .cache()
                    .map {
                        it.steps.toSet()
                    }
                    .cache()
                    .concatMap {
                        stepProgressPublisher.subscribe(it)
                    }

    class LessonIsNotCachedException : Exception("lesson is not in database yet")

}
