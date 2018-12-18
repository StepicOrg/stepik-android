package org.stepik.android.data.personal_deadlines.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesCacheDataSource {
    fun getClosestDeadlineTimestamp(): Single<Long>

    fun getDeadlineRecordsForTimestamp(timestamps: LongArray): Single<List<DeadlineEntity>>
    fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>

    fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Completable

    fun removeDeadlineRecord(recordId: Long): Completable
    fun removeAllDeadlineRecords(): Completable
}