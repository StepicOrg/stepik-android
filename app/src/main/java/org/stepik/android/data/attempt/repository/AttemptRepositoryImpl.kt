package org.stepik.android.data.attempt.repository

import io.reactivex.Single
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.model.attempts.Attempt
import javax.inject.Inject

class AttemptRepositoryImpl
@Inject
constructor(
    private val attemptRemoteDataSource: AttemptRemoteDataSource
) : AttemptRepository {
    override fun createAttemptForStep(stepId: Long): Single<Attempt> =
        attemptRemoteDataSource.createAttemptForStep(stepId)

    override fun getAttemptsForStep(stepId: Long): Single<List<Attempt>> =
        attemptRemoteDataSource.getAttemptsForStep(stepId)

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        attemptRemoteDataSource.getAttempts(*attemptIds)


}