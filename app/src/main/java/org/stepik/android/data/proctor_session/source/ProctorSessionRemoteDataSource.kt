package org.stepik.android.data.proctor_session.source

import io.reactivex.Single
import org.stepik.android.domain.proctor_session.model.ProctorSession

interface ProctorSessionRemoteDataSource {
    fun getProctorSession(id: Long): Single<ProctorSession>
}