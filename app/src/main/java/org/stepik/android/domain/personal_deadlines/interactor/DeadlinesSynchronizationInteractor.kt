package org.stepik.android.domain.personal_deadlines.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepic.droid.model.CourseListType
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import javax.inject.Inject

class DeadlinesSynchronizationInteractor
@Inject
constructor(
    private val courseListRepository: CourseListRepository,
    private val deadlinesRepository: DeadlinesRepository,
    private val deadlinesNotificationsManager: DeadlinesNotificationsManager
) {

    fun syncPersonalDeadlines(): Completable =
        deadlinesRepository
            .removeAllCachedDeadlineRecords()
            .andThen(courseListRepository.getCourseList(CourseListType.ENROLLED))
            .flatMapCompletable { enrolledCourses ->
                Maybe
                    .concat(enrolledCourses.map { deadlinesRepository.getDeadlineRecordByCourseId(it.id) })
                    .ignoreElements()
            }
            .doOnComplete { deadlinesNotificationsManager.scheduleDeadlinesNotifications() }
}