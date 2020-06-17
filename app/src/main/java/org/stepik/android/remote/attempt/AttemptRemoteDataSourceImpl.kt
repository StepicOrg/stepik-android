package org.stepik.android.remote.attempt

import io.reactivex.Single
import io.reactivex.functions.Function
import ru.nobird.android.domain.rx.first
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.attempt.model.AttemptRequest
import org.stepik.android.remote.attempt.model.AttemptResponse
import org.stepik.android.remote.attempt.service.AttemptService
import javax.inject.Inject

class AttemptRemoteDataSourceImpl
@Inject
constructor(
    private val attemptService: AttemptService
) : AttemptRemoteDataSource {
    private val attemptMapper = Function(AttemptResponse::attempts)

    override fun createAttemptForStep(stepId: Long): Single<Attempt> =
        attemptService.createNewAttempt(AttemptRequest(stepId))
            .map(attemptMapper)
            .first()

    override fun getAttemptsForStep(stepId: Long, userId: Long): Single<List<Attempt>> =
        attemptService.getAttemptsForStep(stepId, userId)
            .map(attemptMapper)

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        attemptService.getAttemptsForStep(attemptIds)
            .map(attemptMapper)
}