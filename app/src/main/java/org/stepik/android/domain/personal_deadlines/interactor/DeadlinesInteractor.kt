package org.stepik.android.domain.personal_deadlines.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesBannerRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.domain.personal_deadlines.resolver.DeadlinesResolver
import org.stepik.android.view.personal_deadlines.notification.DeadlinesNotificationDelegate
import javax.inject.Inject

class DeadlinesInteractor
@Inject
constructor(
    private val deadlinesRepository: DeadlinesRepository,
    private val deadlinesBannerRepository: DeadlinesBannerRepository,
    private val deadlinesResolver: DeadlinesResolver,
    private val deadlinesNotificationDelegate: DeadlinesNotificationDelegate
) {
    fun createPersonalDeadlines(courseId: Long, learningRate: LearningRate): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesResolver
            .calculateDeadlinesForCourse(courseId, learningRate)
            .flatMap(deadlinesRepository::createDeadlineRecord)
            .doOnSuccess { deadlinesNotificationDelegate.scheduleDeadlinesNotifications() }

    fun updatePersonalDeadlines(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesRepository
            .updateDeadlineRecord(record)
            .doOnSuccess { deadlinesNotificationDelegate.scheduleDeadlinesNotifications() }

    fun removePersonalDeadline(recordId: Long): Completable =
        deadlinesRepository
            .removeDeadlineRecord(recordId)
            .doOnComplete { deadlinesNotificationDelegate.scheduleDeadlinesNotifications() }

    fun getPersonalDeadlineByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
        deadlinesRepository
            .getDeadlineRecordByCourseId(courseId)

    fun shouldShowDeadlinesBannerForCourse(courseId: Long): Single<Boolean> =
        deadlinesBannerRepository
            .hasCourseId(courseId)
            .doCompletableOnSuccess {
                deadlinesBannerRepository.addCourseId(courseId)
            }
}