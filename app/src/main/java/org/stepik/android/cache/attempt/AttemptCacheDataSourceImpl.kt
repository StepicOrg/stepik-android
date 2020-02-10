package org.stepik.android.cache.attempt

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.attempt.structure.DbStructureAttempt
import org.stepik.android.data.attempt.source.AttemptCacheDataSource
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class AttemptCacheDataSourceImpl
@Inject
constructor(
    private val attemptDao: IDao<Attempt>
) : AttemptCacheDataSource {
    override fun getAttemptsForStep(stepId: Long): Single<List<Attempt>> =
        Single
            .fromCallable {
                attemptDao.getAll(DbStructureAttempt.Columns.STEP, stepId.toString())
            }

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        Single
            .fromCallable {
                if (attemptIds.isNotEmpty()) {
                    attemptDao.getAllInRange(DbStructureAttempt.Columns.ID, attemptIds.joinToString())
                } else {
                    attemptDao.getAll()
                }
            }

    override fun saveAttempts(items: List<Attempt>): Completable =
        Completable
            .fromAction {
                attemptDao.insertOrReplaceAll(items)
            }
}