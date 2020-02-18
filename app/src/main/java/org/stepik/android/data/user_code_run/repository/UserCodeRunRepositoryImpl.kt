package org.stepik.android.data.user_code_run.repository

import io.reactivex.Single
import org.stepik.android.data.user_code_run.source.UserCodeRunRemoteDataSource
import org.stepik.android.domain.user_code_run.repository.UserCodeRunRepository
import org.stepik.android.model.code.UserCodeRun
import javax.inject.Inject

class UserCodeRunRepositoryImpl
@Inject constructor(
    private val userCodeRunRemoteDataSource: UserCodeRunRemoteDataSource
) : UserCodeRunRepository {
    override fun createUserCodeRun(userCodeRun: UserCodeRun): Single<UserCodeRun> =
        userCodeRunRemoteDataSource.createUserCodeRun(userCodeRun)

    override fun getUserCodeRun(userCodeRunId: Long): Single<UserCodeRun> =
        userCodeRunRemoteDataSource.getUserCodeRun(userCodeRunId)
}