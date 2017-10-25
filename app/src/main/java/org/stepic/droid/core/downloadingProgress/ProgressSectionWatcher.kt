package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.RetryWithDelay
import javax.inject.Inject

class ProgressSectionWatcher
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val stepProgressPublisher: StepProgressPublisher) : ProgressWatcher {

    companion object {
        private const val RETRY_DELAY = 300
    }

    override fun watch(id: Long): Flowable<Float> =
            Flowable
                    .fromCallable {
                        databaseFacade.getSectionById(id)
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //wait, when section will be in database
                    .cache()
                    .flatMapIterable {
                        val unitsFromDatabase = databaseFacade.getUnitsByIds(it.units)
                        if (unitsFromDatabase.size != it.units?.size) {
                            throw UnitsAreNotCachedException()
                        }
                        unitsFromDatabase
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //wait until all units will be in database
                    .map {
                        it.lesson
                    }
                    .toList() //get all fields 'lesson' in units
                    .toFlowable()
                    .flatMapIterable {
                        val lessonIds = it.toLongArray()
                        val lessons = databaseFacade.getLessonsByIds(lessonIds)
                        if (lessonIds.size != lessons.size) {
                            throw LessonsAreNotCachedException()
                        }
                        lessons
                    }
                    .retryWhen(RetryWithDelay(RETRY_DELAY)) //wait until all lessons will be in database
                    .map { it.steps.toMutableList() }
                    .reduce { accumulator: MutableList<Long>, current: MutableList<Long> ->
                        accumulator.addAll(current)
                        accumulator
                    }
                    .toFlowable()
                    .map { it.toSet() }
                    .cache() // cache set of steps
                    .concatMap {
                        stepProgressPublisher.subscribe(it)
                    }


    class UnitsAreNotCachedException : Exception("Units are not in database yet")
    class LessonsAreNotCachedException : Exception("Lessons are not in database yet")
}
