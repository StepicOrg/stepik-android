package org.stepik.android.domain.personal_deadlines.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.domain.personal_deadlines.resolver.DeadlinesResolverImpl
import org.stepic.droid.model.CourseListType
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesBannerRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import javax.inject.Inject

class DeadlinesInteractor
@Inject
constructor(
    private val courseListRepository: CourseListRepository,
    private val deadlinesRepository: DeadlinesRepository,
    private val deadlinesBannerRepository: DeadlinesBannerRepository,
    private val deadlinesResolver: DeadlinesResolver,
    private val deadlinesNotificationsManager: DeadlinesNotificationsManager
) {
    fun createPersonalDeadlines(courseId: Long, learningRate: LearningRate): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesResolver
            .calculateDeadlinesForCourse(courseId, learningRate)
            .flatMap(deadlinesRepository::createDeadlineRecord)
            .doOnSuccess { deadlinesNotificationsManager.scheduleDeadlinesNotifications() }

    fun updatePersonalDeadlines(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesRepository
            .updateDeadlineRecord(record)
            .doOnSuccess { deadlinesNotificationsManager.scheduleDeadlinesNotifications() }

    fun removePersonalDeadline(recordId: Long): Completable =
        deadlinesRepository
            .removeDeadlineRecord(recordId)
            .doOnComplete { deadlinesNotificationsManager.scheduleDeadlinesNotifications() }

    fun getPersonalDeadlineByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
        deadlinesRepository
            .getDeadlineRecordByCourseId(courseId)


    fun syncPersonalDeadlines(): Completable =
        deadlinesRepository
            .removeAllCachedDeadlineRecords()
            .andThen(courseListRepository.getCourseList(CourseListType.ENROLLED))
            .flatMapCompletable { enrolledCourses ->
                Maybe
                    .concat(enrolledCourses.map { deadlinesRepository.getDeadlineRecordByCourseId(it.id) })
                    .ignoreElements()
            }

    fun shouldShowDeadlinesBannerForCourse(courseId: Long): Single<Boolean> =
        deadlinesBannerRepository
            .hasCourseId(courseId)
            .doCompletableOnSuccess {
                deadlinesBannerRepository.addCourseId(courseId)
            }
}