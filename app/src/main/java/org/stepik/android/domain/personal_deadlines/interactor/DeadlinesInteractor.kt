package org.stepik.android.domain.personal_deadlines.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.domain.personal_deadlines.model.LearningRate
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesBannerRepository
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import org.stepik.android.domain.personal_deadlines.resolver.DeadlinesResolver
import org.stepik.android.view.notification.delegate.DeadlinesDelegate
import javax.inject.Inject

class DeadlinesInteractor
@Inject
constructor(
    private val deadlinesRepository: DeadlinesRepository,
    private val deadlinesBannerRepository: DeadlinesBannerRepository,
    private val deadlinesResolver: DeadlinesResolver,
    private val deadlinesDelegate: DeadlinesDelegate
) {
    fun createPersonalDeadlines(courseId: Long, learningRate: LearningRate): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesResolver
            .calculateDeadlinesForCourse(courseId, learningRate)
            .flatMap(deadlinesRepository::createDeadlineRecord)
            .doOnSuccess { deadlinesDelegate.scheduleDeadlinesNotifications() }

    fun updatePersonalDeadlines(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesRepository
            .updateDeadlineRecord(record)
            .doOnSuccess { deadlinesDelegate.scheduleDeadlinesNotifications() }

    fun removePersonalDeadline(recordId: Long): Completable =
        deadlinesRepository
            .removeDeadlineRecord(recordId)
            .doOnComplete { deadlinesDelegate.scheduleDeadlinesNotifications() }

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