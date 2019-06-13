package org.stepik.android.domain.personal_deadlines.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.model.CourseListType
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.view.personal_deadlines.notification.DeadlinesNotificationDelegate
import javax.inject.Inject

class DeadlinesSynchronizationInteractor
@Inject
constructor(
    private val courseListRepository: CourseListRepository,
    private val deadlinesRepository: DeadlinesRepository,
    private val deadlinesNotificationDelegate: DeadlinesNotificationDelegate
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
            .doOnComplete { deadlinesNotificationDelegate.scheduleDeadlinesNotifications() }
}