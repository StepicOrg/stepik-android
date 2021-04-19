package org.stepik.android.remote.proctor_session

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.proctor_session.source.ProctorSessionRemoteDataSource
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.remote.proctor_session.model.ProctorSessionResponse
import org.stepik.android.remote.proctor_session.service.ProctorSessionService
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class ProctorSessionRemoteDataSourceImpl
@Inject
constructor(
    private val proctorSessionService: ProctorSessionService
) : ProctorSessionRemoteDataSource {
    private val proctorSessionMapper = Function(ProctorSessionResponse::proctorSessions)

    override fun getProctorSession(id: Long): Single<ProctorSession> =
        proctorSessionService
            .getProctorSession(id)
            .map(proctorSessionMapper)
            .first()
}