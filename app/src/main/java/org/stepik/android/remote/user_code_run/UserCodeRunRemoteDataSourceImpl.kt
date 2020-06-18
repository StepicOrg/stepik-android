package org.stepik.android.remote.user_code_run

import io.reactivex.Single
import io.reactivex.functions.Function
import ru.nobird.android.domain.rx.first
import org.stepik.android.data.user_code_run.source.UserCodeRunRemoteDataSource
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.remote.user_code_run.model.UserCodeRunRequest
import org.stepik.android.remote.user_code_run.model.UserCodeRunResponse
import org.stepik.android.remote.user_code_run.service.UserCodeRunService
import javax.inject.Inject

class UserCodeRunRemoteDataSourceImpl
@Inject constructor(
    private val userCodeRunService: UserCodeRunService
) : UserCodeRunRemoteDataSource {
    private val userCodeRunMapper = Function(UserCodeRunResponse::userCodeRuns)

    override fun createUserCodeRun(userCodeRun: UserCodeRun): Single<UserCodeRun> =
        userCodeRunService.createUserCodeRun(UserCodeRunRequest(userCodeRun))
            .map(userCodeRunMapper)
            .first()

    override fun getUserCodeRun(userCodeRunId: Long): Single<UserCodeRun> =
        userCodeRunService.getUserCodeRuns(userCodeRunId)
            .map(userCodeRunMapper)
            .first()
}