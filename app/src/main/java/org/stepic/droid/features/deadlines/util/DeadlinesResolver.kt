package org.stepic.droid.features.deadlines.util

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepik.android.model.structure.Section
import org.stepic.droid.features.deadlines.model.Deadline
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.model.LearningRate
import org.stepic.droid.util.AppConstants
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
    }

    fun calculateDeadlinesForCourse(courseId: Long, learningRate: LearningRate): Single<DeadlinesWrapper> =
            api.getCoursesReactive(1, longArrayOf(courseId)).flatMap {
                api.getSectionsRx(it.courses.first().sections)
            }.flatMapObservable {
                it.sections.toObservable()
            }.concatMapEager(::getTimeToCompleteForSection).toList().map {
                val offset = Calendar.getInstance()
                offset.add(Calendar.HOUR_OF_DAY, 24)
                offset.set(Calendar.HOUR_OF_DAY, 0)
                offset.set(Calendar.MINUTE, 0)

                val deadlines = it.map { (sectionId, timeToComplete) ->
                    val deadlineDate = getDeadlineDate(offset, timeToComplete, learningRate)
                    Deadline(sectionId, deadlineDate)
                }
                DeadlinesWrapper(courseId, deadlines)
            }

    private fun getTimeToCompleteForSection(section: Section): Observable<Pair<Long, Long>> =
            section.units.asIterable().chunked(CHUNK_SIZE).toObservable().flatMap {
                api.getUnitsRx(it.toLongArray()).toObservable()
            }.flatMap {
                val ids = it.units?.map { it.lesson }?.toLongArray()
                api.getLessonsRx(ids).flatMapObservable {
                    it.lessons?.toObservable()
                }
            }.reduce(0L) { acc, lesson ->
                acc + if (lesson.timeToComplete == 0L) lesson.steps.size * DEFAULT_STEP_LENGTH_IN_SECONDS else lesson.timeToComplete
            }.map { section.id to it }.toObservable()

    private fun getDeadlineDate(calendar: Calendar, timeToComplete: Long, learningRate: LearningRate): Date {
        val timePerWeek = learningRate.millisPerWeek

        val time = timeToComplete * 1000 * TIME_MULTIPLIER / timePerWeek * AppConstants.MILLIS_IN_SEVEN_DAYS
        calendar.timeInMillis += time.toLong()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val date =  calendar.time
        calendar.add(Calendar.MINUTE, 1) // set time at 00:00 of the next day
        return date
    }
}