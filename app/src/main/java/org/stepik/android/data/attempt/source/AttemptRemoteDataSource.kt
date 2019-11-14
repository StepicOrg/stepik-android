package org.stepik.android.data.attempt.source

import io.reactivex.Single
import org.stepik.android.model.attempts.Attempt

interface AttemptRemoteDataSource {
    fun createAttemptForStep(stepId: Long): Single<Attempt>
    fun getAttemptsForStep(stepId: Long): Single<List<Attempt>>

    fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>>
}