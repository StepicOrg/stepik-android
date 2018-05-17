package org.stepic.droid.storage.operations

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.model.deadlines.DeadlineFlatItem
import org.stepic.droid.model.deadlines.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRecordOperations {
    fun getClosestDeadlineTimestamp(): Single<Long>

    fun getDeadlineRecordForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>
    fun getDeadlineRecordsForTimestamp(timestamp: Long): Single<List<DeadlineFlatItem>>

    fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>
    fun removeDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Completable
    fun removeAllDeadlineRecords(): Completable
}