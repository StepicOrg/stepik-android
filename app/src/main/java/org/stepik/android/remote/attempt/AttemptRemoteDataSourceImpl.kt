package org.stepik.android.remote.attempt

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.first
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.attempt.model.AttemptRequest
import org.stepik.android.remote.attempt.model.AttemptResponse
import org.stepik.android.remote.attempt.service.AttemptService
import javax.inject.Inject

class AttemptRemoteDataSourceImpl
@Inject
constructor(
    private val attemptService: AttemptService,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : AttemptRemoteDataSource {
    private val attemptMapper = Function(AttemptResponse::attempts)

    override fun createAttemptForStep(stepId: Long): Single<Attempt> =
        attemptService.createNewAttemptReactive(AttemptRequest(stepId))
            .map(attemptMapper)
            .first()

    override fun getAttemptsForStep(stepId: Long): Single<List<Attempt>> =
        attemptService.getExistingAttemptsReactive(stepId, sharedPreferenceHelper.profile?.id ?: 0)
            .map(attemptMapper)

    override fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>> =
        attemptService.getExistingAttemptsReactive(attemptIds)
            .map(attemptMapper)
}