package org.stepik.android.data.attempt.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.attempt.source.AttemptCacheDataSource
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.domain.attempt.repository.AttemptRepository
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
            .getAttemptsForStep(stepId)
            .doCompletableOnSuccess(attemptCacheDataSource::saveAttempts)
            .onErrorResumeNext(attemptCacheDataSource.getAttemptsForStep(stepId, userId))

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        attemptRemoteDataSource
            .getAttempts(*attemptIds)
            .doCompletableOnSuccess(attemptCacheDataSource::saveAttempts)
            .onErrorResumeNext(attemptCacheDataSource.getAttempts(*attemptIds))
}