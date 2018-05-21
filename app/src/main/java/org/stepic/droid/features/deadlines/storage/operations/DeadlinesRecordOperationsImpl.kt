package org.stepic.droid.features.deadlines.storage.operations

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.di.storage.StorageSingleton
import org.stepic.droid.features.deadlines.model.Deadline
import org.stepic.droid.features.deadlines.model.DeadlineFlatItem
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper
import org.stepic.droid.features.deadlines.storage.DbStructureDeadlines
import org.stepic.droid.features.deadlines.storage.dao.PersonalDeadlinesDao
import org.stepic.droid.features.deadlines.util.getKindOfRecord
import org.stepic.droid.util.AppConstants
import org.stepic.droid.web.storage.model.StorageRecord
import java.util.*
import javax.inject.Inject

@StorageSingleton
class DeadlinesRecordOperationsImpl
@Inject
constructor(
        private val personalDeadlinesDao: PersonalDeadlinesDao
): DeadlinesRecordOperations {
    companion object {
        private fun StorageRecord<DeadlinesWrapper>.flatten(): List<DeadlineFlatItem> =
                data.deadlines.map {
                    DeadlineFlatItem(this.id
                            ?: -1, this.data.course, it.section, it.deadline)
                }

        private const val DEFAULT_GAP = AppConstants.MILLIS_IN_1HOUR
    }

    override fun getClosestDeadlineTimestamp(): Single<Long> = Single.create { emitter ->
        emitter.onSuccess(personalDeadlinesDao.getClosestDeadlineDate()?.time ?: 0)
    }

    override fun getDeadlineRecordForCourse(courseId: Long): Maybe<StorageRecord<DeadlinesWrapper>> = Maybe.create { emitter ->
        val items = personalDeadlinesDao.getAll(DbStructureDeadlines.Columns.COURSE_ID, courseId.toString()).filterNotNull()
        if (items.isNotEmpty()) {
            val recordId: Long = items.first().recordId
            val deadlines = items.filter { it.recordId == recordId }.map {
                Deadline(it.sectionId, it.deadline)
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

    override fun getDeadlineRecordsForTimestamp(timestamps: LongArray): Single<List<DeadlineFlatItem>> = Single.fromCallable {
        timestamps.map { timestamp ->
            personalDeadlinesDao.getDeadlinesBetween(Date(timestamp - DEFAULT_GAP), Date(timestamp + DEFAULT_GAP))
        }.flatten()
    }

    override fun saveDeadlineRecord(record: StorageRecord<DeadlinesWrapper>): Single<StorageRecord<DeadlinesWrapper>> = Single.create { emitter ->
        personalDeadlinesDao.insertOrReplaceAll(record.flatten())
        emitter.onSuccess(record)
    }

    override fun removeDeadlineRecord(recordId: Long): Completable = Completable.fromCallable {
        personalDeadlinesDao.remove(DbStructureDeadlines.Columns.RECORD_ID, recordId.toString())
    }

    override fun removeAllDeadlineRecords(): Completable =
            Completable.fromCallable(personalDeadlinesDao::removeAll)
}