package org.stepik.android.data.personal_deadlines.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesCacheDataSource {
    fun getClosestDeadlineTimestamp(): Single<Long>

    fun getDeadlineRecordForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>
    fun getDeadlineRecordsForTimestamp(timestamps: LongArray): Single<List<DeadlineEntity>>

    fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>
    fun removeDeadlineRecord(recordId: Long): Completable
    fun removeAllDeadlineRecords(): Completable
}