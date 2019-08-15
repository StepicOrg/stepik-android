package org.stepik.android.remote.personal_deadlines

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.toMaybe
import org.stepic.droid.web.storage.RemoteStorageService
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.data.personal_deadlines.getKindOfRecord
import org.stepik.android.data.personal_deadlines.getKindStartsWithOfRecord
import org.stepik.android.data.personal_deadlines.source.DeadlinesRemoteDataSource
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import org.stepik.android.remote.personal_deadlines.mapper.DeadlinesMapper
import javax.inject.Inject

class DeadlinesRemoteDataSourceImpl
@Inject
constructor(
    private val remoteStorageService: RemoteStorageService,
    private val deadlinesMapper: DeadlinesMapper,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : DeadlinesRemoteDataSource {

    override fun createDeadlineRecord(deadlines: DeadlinesWrapper): Single<StorageRecord<DeadlinesWrapper>> =
        remoteStorageService
            .createStorageRecord(deadlinesMapper.mapToStorageRequest(deadlines))
            .map(deadlinesMapper::mapToStorageRecord)

    override fun updateDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> =
        remoteStorageService
            .setStorageRecord(record.id ?: 0, deadlinesMapper.mapToStorageRequest(record))
            .map(deadlinesMapper::mapToStorageRecord)

    override fun removeDeadlineRecord(recordId: Long): Completable =
        remoteStorageService
            .removeStorageRecord(recordId)

    override fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
        remoteStorageService
            .getStorageRecords(1, sharedPreferenceHelper.profile?.id ?: -1, kind = getKindOfRecord(courseId))
            .firstElement()
            .flatMap { response ->
                deadlinesMapper
                    .mapToStorageRecord(response)
                    .toMaybe()
            }

    override fun getDeadlinesRecords(): Maybe<List<StorageRecord<DeadlinesWrapper>>> =
        remoteStorageService
            .getStorageRecords(1, sharedPreferenceHelper.profile?.id ?: -1, startsWith = getKindStartsWithOfRecord())
            .firstElement()
            .flatMap { response ->
                deadlinesMapper
                    .mapToStorageRecordList(response)
                    .toMaybe()
            }
}