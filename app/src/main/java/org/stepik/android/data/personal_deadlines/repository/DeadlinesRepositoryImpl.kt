package org.stepik.android.data.personal_deadlines.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.then
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepik.android.data.personal_deadlines.source.DeadlinesRemoteDataSource
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesRepository
import javax.inject.Inject

class DeadlinesRepositoryImpl
@Inject
constructor(
    private val deadlinesRemoteDataSource: DeadlinesRemoteDataSource,
    private val deadlinesCacheDataSource: DeadlinesCacheDataSource
) : DeadlinesRepository {
    override fun createDeadlineRecord(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesRemoteDataSource
            .createDeadlineRecord(deadlines)
            .doCompletableOnSuccess(deadlinesCacheDataSource::saveDeadlineRecord)

    override fun updateDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
        deadlinesRemoteDataSource
            .updateDeadlineRecord(record)
            .doCompletableOnSuccess(deadlinesCacheDataSource::saveDeadlineRecord)

    override fun removeDeadlineRecord(recordId: Long): Completable =
        deadlinesCacheDataSource.removeDeadlineRecord(recordId) then
            deadlinesRemoteDataSource.removeDeadlineRecord(recordId)

    override fun removeDeadlineRecordByCourseId(courseId: Long): Completable =
        deadlinesRemoteDataSource
            .getDeadlineRecordByCourseId(courseId)
            .flatMapCompletable { removeDeadlineRecord(it.id!!) }

    override fun removeAllCachedDeadlineRecords(): Completable =
        deadlinesCacheDataSource.removeAllDeadlineRecords()

    override fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
        deadlinesRemoteDataSource
            .getDeadlineRecordByCourseId(courseId)
            .doCompletableOnSuccess(deadlinesCacheDataSource::saveDeadlineRecord)

    override fun getDeadlineRecords(): Maybe<List<StorageRecord<DeadlinesWrapper>>> =
        deadlinesRemoteDataSource
            .getDeadlinesRecords()
            .doCompletableOnSuccess(deadlinesCacheDataSource::saveDeadlineRecords)
}