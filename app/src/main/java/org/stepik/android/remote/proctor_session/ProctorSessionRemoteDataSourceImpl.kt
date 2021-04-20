package org.stepik.android.remote.proctor_session

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.proctor_session.source.ProctorSessionRemoteDataSource
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.remote.proctor_session.model.ProctorSessionResponse
import org.stepik.android.remote.proctor_session.service.ProctorSessionService
import javax.inject.Inject

class ProctorSessionRemoteDataSourceImpl
@Inject
constructor(
    private val proctorSessionService: ProctorSessionService
) : ProctorSessionRemoteDataSource {
    private val proctorSessionMapper = Function(ProctorSessionResponse::proctorSessions)

    override fun getProctorSessions(ids: List<Long>): Single<List<ProctorSession>> =
        proctorSessionService
            .getProctorSessions(ids)
            .map(proctorSessionMapper)
}