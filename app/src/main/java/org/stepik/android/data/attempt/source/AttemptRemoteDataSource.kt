package org.stepik.android.data.attempt.source

import io.reactivex.Single
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.remote.attempt.model.AttemptResponse

interface AttemptRemoteDataSource {
    fun createAttemptForStep(stepId: Long): Single<Attempt>
    fun getAttemptsForStep(stepId: Long): Single<List<Attempt>>

    fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>>

    // TODO Remove, ask about Card.kt, line 89
    fun createAttemptForStepAdaptive(stepId: Long): Single<AttemptResponse>
    fun getAttemptsForStepAdaptive(stepId: Long): Single<AttemptResponse>
}