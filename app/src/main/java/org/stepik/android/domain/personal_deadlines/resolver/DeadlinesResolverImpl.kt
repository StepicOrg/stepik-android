package org.stepik.android.domain.personal_deadlines.resolver

import io.reactivex.Single
import org.stepic.droid.util.AppConstants
import ru.nobird.android.core.model.mapToLongArray
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DeadlinesResolverImpl
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository
) : DeadlinesResolver {
    companion object {
        private const val DEFAULT_STEP_LENGTH_IN_SECONDS = 60L
        private const val TIME_MULTIPLIER = 1.3
    }

    override fun calculateDeadlinesForCourse(courseId: Long, learningRate: LearningRate): Single<DeadlinesWrapper> =
        courseRepository.getCourse(courseId)
            .flatMapSingle { course ->
                sectionRepository.getSections(course.sections ?: listOf())
            }
            .flatMap { sections ->
                val unitIds = sections
                    .flatMap(Section::units)
                    .fold(listOf(), List<Long>::plus)

                unitRepository
                    .getUnits(unitIds)
                    .map { units -> sections to units }
            }
            .flatMap { (sections, units) ->
                val lessonIds = units.mapToLongArray(Unit::lesson)
                lessonRepository
                    .getLessons(*lessonIds)
                    .map { lessons ->
                        sections.map { section ->
                            getTimeToCompleteForSection(section, units, lessons)
                        }
                    }
            }
            .map {
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

    private fun getTimeToCompleteForSection(section: Section, units: List<Unit>, lessons: List<Lesson>): Pair<Long, Long> =
        section
            .units
            .fold(0L) { acc, unitId ->
                val unit = units.find { it.id == unitId }

                val timeToComplete: Long = lessons
                    .find { it.id == unit?.lesson }
                    ?.let { lesson ->
                        lesson
                            .timeToComplete
                            .takeIf { it != 0L }
                            ?: lesson.steps.size * DEFAULT_STEP_LENGTH_IN_SECONDS
                    }
                    ?: 0L

                acc + timeToComplete
            }
            .let {
                section.id to it
            }

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