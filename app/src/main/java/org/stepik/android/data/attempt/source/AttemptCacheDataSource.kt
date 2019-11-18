package org.stepik.android.data.attempt.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.attempts.Attempt

interface AttemptCacheDataSource {
    fun getAttemptsForStep(stepId: Long): Single<List<Attempt>>

    fun getAttempts(vararg attemptIds: Long): Single<List<Attempt>>

    fun saveAttempts(items: List<Attempt>): Completable
}