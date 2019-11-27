package org.stepik.android.domain.personal_deadlines.interactor

import org.stepic.droid.util.AppConstants
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import javax.inject.Inject

class DeadlinesNotificationInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val sectionRepository: SectionRepository,
    private val deadlinesCacheDataSource: DeadlinesCacheDataSource
) {
    companion object {
        private const val OFFSET_12HOURS = 12 * AppConstants.MILLIS_IN_1HOUR
        private const val OFFSET_36HOURS = 36 * AppConstants.MILLIS_IN_1HOUR
    }

    fun getCourse(courseId: Long): Course? =
        courseRepository.getCourses(courseId).blockingGet().firstOrNull()

    fun getSection(sectionId: Long): Section? =
        sectionRepository.getSections(sectionId).blockingGet().firstOrNull()

    fun getDeadlineRecordsForTimestamp(now: Long) =
        deadlinesCacheDataSource.getDeadlineRecordsForTimestamp(longArrayOf(now + OFFSET_12HOURS, now + OFFSET_36HOURS))

    fun getClosestDeadlineTimestamp(): Long =
        deadlinesCacheDataSource
            .getClosestDeadlineTimestamp()
            .onErrorReturnItem(0)
            .blockingGet()
}