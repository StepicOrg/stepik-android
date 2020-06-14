package org.stepik.android.data.user_code_run.source

import io.reactivex.Single
import org.stepik.android.model.code.UserCodeRun

interface UserCodeRunRemoteDataSource {
    fun createUserCodeRun(userCodeRun: UserCodeRun): Single<UserCodeRun>
    fun getUserCodeRun(userCodeRunId: Long): Single<UserCodeRun>
}