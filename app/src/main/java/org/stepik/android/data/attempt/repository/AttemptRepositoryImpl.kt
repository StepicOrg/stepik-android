package org.stepik.android.data.attempt.repository

import io.reactivex.Single
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepik.android.data.attempt.source.AttemptCacheDataSource
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class AttemptRepositoryImpl
@Inject
constructor(
    private val attemptRemoteDataSource: AttemptRemoteDataSource,
    private val attemptCacheDataSource: AttemptCacheDataSource
) : AttemptRepository {
    override fun createAttemptForStep(stepId: Long): Single<Attempt> =
        attemptRemoteDataSource
            .createAttemptForStep(stepId)
            .doCompletableOnSuccess { attemptCacheDataSource.saveAttempts(listOf(it)) }

    override fun getAttemptsForStep(stepId: Long, userId: Long): Single<List<Attempt>> =
        attemptRemoteDataSource
            .getAttemptsForStep(stepId, userId)
            .doCompletableOnSuccess(attemptCacheDataSource::saveAttempts)
            .onErrorResumeNext(attemptCacheDataSource.getAttemptsForStep(stepId))

    override fun getAttempts(vararg attemptIds: Long, dataSourceType: DataSourceType): Single<List<Attempt>> =
        if (attemptIds.isEmpty()) {
            Single.just(emptyList())
        } else {
            when (dataSourceType) {
                DataSourceType.CACHE ->
                    attemptCacheDataSource.getAttempts(*attemptIds)

                DataSourceType.REMOTE ->
                    attemptRemoteDataSource
                        .getAttempts(*attemptIds)
                        .doCompletableOnSuccess(attemptCacheDataSource::saveAttempts)
                        .onErrorResumeNext(attemptCacheDataSource.getAttempts(*attemptIds))
            }
        }
}