package org.stepik.android.domain.personal_deadlines.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRepository {
    fun createDeadlineRecord(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>>
    fun updateDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>

    fun removeAllCachedDeadlineRecords(): Completable
    fun removeDeadlineRecord(recordId: Long): Completable
    fun removeDeadlineRecordByCourseId(courseId: Long): Completable
    fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>
}