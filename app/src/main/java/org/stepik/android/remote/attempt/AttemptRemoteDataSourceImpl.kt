package org.stepik.android.remote.attempt

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.util.maybeFirst
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
            .maybeFirst()
            .toSingle()

    override fun getAttemptsForStep(stepId: Long): Single<List<Attempt>> =
        api.getExistingAttemptsReactive(stepId)
            .map(attemptMapper)
}