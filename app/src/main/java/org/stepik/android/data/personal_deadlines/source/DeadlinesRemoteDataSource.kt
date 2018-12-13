package org.stepik.android.data.personal_deadlines.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.web.storage.model.StorageRecord

interface DeadlinesRemoteDataSource {

    fun createDeadlineRecord(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>>
    fun updateDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>>

    fun removeDeadlineRecord(recordId: Long): Completable
    fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>>

}