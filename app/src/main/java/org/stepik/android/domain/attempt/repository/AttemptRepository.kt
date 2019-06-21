package org.stepik.android.domain.attempt.repository

import io.reactivex.Single
import org.stepik.android.model.attempts.Attempt

interface AttemptRepository {
    fun createAttemptForStep(stepId: Long): Single<Attempt>
    fun getAttemptsForStep(stepId: Long): Single<List<Attempt>>
}