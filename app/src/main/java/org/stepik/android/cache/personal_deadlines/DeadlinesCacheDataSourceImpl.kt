package org.stepik.android.cache.personal_deadlines

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.AppConstants
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDao
import org.stepik.android.cache.personal_deadlines.mapper.DeadlineEntityMapper
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlines
import org.stepik.android.data.personal_deadlines.getKindOfRecord
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepik.android.domain.personal_deadlines.model.Deadline
import org.stepik.android.domain.personal_deadlines.model.DeadlinesWrapper
import java.util.Date
import javax.inject.Inject

class DeadlinesCacheDataSourceImpl
@Inject
constructor(
    private val personalDeadlinesDao: PersonalDeadlinesDao,
    private val deadlinesEntityMapper: DeadlineEntityMapper
) : DeadlinesCacheDataSource {
    companion object {
        private const val DEFAULT_GAP = AppConstants.MILLIS_IN_1HOUR
    }

    override fun getClosestDeadlineTimestamp(): Single<Long> =
        Single.fromCallable {
            personalDeadlinesDao.getClosestDeadlineDate()?.time ?: 0
        }

    override fun getDeadlineRecordByCourseId(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> =
        Maybe.create { emitter ->
            val items = personalDeadlinesDao
                .getAll(DbStructureDeadlines.Columns.COURSE_ID, courseId.toString())
                .filterNotNull()

            if (items.isNotEmpty()) {
                val recordId: Long = items.first().recordId
                val deadlines = items
                    .filter { it.recordId == recordId }
                    .map { entity ->
                        Deadline(entity.sectionId, entity.deadline)
                    }

                emitter.onSuccess(StorageRecord(
                    id = recordId,
                    kind = getKindOfRecord(courseId),
                    data = DeadlinesWrapper(courseId, deadlines)
                ))
            } else {
                emitter.onComplete()
            }
        }

    override fun getDeadlineRecordsForTimestamp(timestamps: LongArray): Single<List<DeadlineEntity>> =
        Single.fromCallable {
            timestamps
                .map { timestamp ->
                    personalDeadlinesDao
                        .getDeadlinesBetween(Date(timestamp - DEFAULT_GAP), Date(timestamp + DEFAULT_GAP))
                }
                .flatten()
        }

    override fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Completable =
        Completable.fromAction {
            personalDeadlinesDao.insertOrReplaceAll(deadlinesEntityMapper.mapToEntity(record))
        }

    override fun saveDeadlineRecords(records: List<StorageRecord<DeadlinesWrapper>>): Completable =
        Completable.fromAction {
            personalDeadlinesDao.insertOrReplaceAll(records.flatMap { deadlinesEntityMapper.mapToEntity(it) })
        }

    override fun removeDeadlineRecord(recordId: Long): Completable =
        Completable.fromCallable {
            personalDeadlinesDao
                .remove(DbStructureDeadlines.Columns.RECORD_ID, recordId.toString())
        }

    override fun removeAllDeadlineRecords(): Completable =
        Completable.fromCallable(personalDeadlinesDao::removeAll)
}