package org.stepik.android.remote.attempt

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.util.first
import org.stepic.droid.web.Api
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.attempt.model.AttemptResponse
import javax.inject.Inject

class AttemptRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : AttemptRemoteDataSource {
    private val attemptMapper = Function(AttemptResponse::attempts)

    override fun createAttemptForStep(stepId: Long): Single<Attempt> =
        api.createNewAttemptReactive(stepId)
            .map(attemptMapper)
            .first()

    override fun getAttemptsForStep(stepId: Long): Single<List<Attempt>> =
        api.getExistingAttemptsReactive(stepId)
            .map(attemptMapper)

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        api.getExistingAttemptsReactive(attemptIds)
            .map(attemptMapper)
}