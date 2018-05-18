package org.stepic.droid.features.deadlines.storage.operations

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.features.deadlines.model.DeadlineFlatItem
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRecordOperations {
    fun getClosestDeadlineTimestamp(): Single<Long>

    fun getDeadlineRecordForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>
    fun getDeadlineRecordsForTimestamp(timestamp: Long): Single<List<DeadlineFlatItem>>

    fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>
    fun removeDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Completable
    fun removeAllDeadlineRecords(): Completable
}