package org.stepik.android.domain.user_code_run.repository

import io.reactivex.Single
import org.stepik.android.model.code.UserCodeRun

interface UserCodeRunRepository {
    fun createUserCodeRun(userCodeRun: UserCodeRun): Single<UserCodeRun>
    fun getUserCodeRun(userCodeRunId: Long): Single<UserCodeRun>
}