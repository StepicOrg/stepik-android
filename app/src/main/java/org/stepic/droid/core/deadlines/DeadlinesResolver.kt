package org.stepic.droid.core.deadlines

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.model.Section
import org.stepic.droid.model.deadlines.Deadline
import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.web.Api
import java.util.*
import javax.inject.Inject

@AppSingleton
class DeadlinesResolver
@Inject
constructor(
        private val api: Api
) {
    companion object {
        private const val CHUNK_SIZE = 100

        private const val DEFAULT_STEP_LENGTH_IN_SECONDS = 60L
        private const val TIME_MULTIPLIER = 1.3

        private const val MILLISECONDS_IN_HOUR = 60 * 60 * 1000
        private const val MILLISECONDS_IN_WEEK = 7 * 24 * MILLISECONDS_IN_HOUR
    }

    fun createDeadlinesForCourse(courseId: Long, hoursPerWeek: Long): Single<DeadlinesWrapper> =
            api.getCoursesReactive(1, longArrayOf(courseId)).flatMap {
                api.getSectionsRx(it.courses.first().sections)
            }.flatMapObservable {
                it.sections.toObservable()
            }.flatMap(::getTimeToCompleteForSection).toList().map {
                val offset = Calendar.getInstance()

                val deadlines = it.map { (sectionId, timeToComplete) ->
                    val deadlineDate = getDeadlineDate(offset, timeToComplete, hoursPerWeek)
                    Deadline(sectionId, deadlineDate)
                }
                DeadlinesWrapper(courseId, deadlines)
            }

    private fun getTimeToCompleteForSection(section: Section): Observable<Pair<Long, Long>> =
            section.units.asIterable().chunked(CHUNK_SIZE).toObservable().flatMap {
                api.getUnitsRx(it.toLongArray()).toObservable()
            }.concatMap {
                val ids = it.units?.map { it.lesson }?.toLongArray()
                api.getLessonsRx(ids).flatMapObservable {
                    it.lessons?.toObservable()
                }
            }.reduce(0L) { acc, lesson ->
                acc + if (lesson.timeToComplete == 0L) lesson.steps.size * DEFAULT_STEP_LENGTH_IN_SECONDS else lesson.timeToComplete
            }.map { section.id to it }.toObservable()

    private fun getDeadlineDate(calendar: Calendar, timeToComplete: Long, hoursPerWeek: Long): Date {
        val timePerWeek = hoursPerWeek * MILLISECONDS_IN_HOUR

        val time = timeToComplete * 1000 * TIME_MULTIPLIER / timePerWeek * MILLISECONDS_IN_WEEK
        calendar.timeInMillis += time.toLong()
        calendar.add(Calendar.DATE, 1) // set time at 00:00 of the next day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        return calendar.time
    }
}