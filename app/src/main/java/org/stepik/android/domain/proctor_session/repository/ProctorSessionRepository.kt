package org.stepik.android.domain.proctor_session.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.proctor_session.model.ProctorSession

interface ProctorSessionRepository {
    fun getProctorSessions(ids: List<Long>, sourceType: DataSourceType = DataSourceType.REMOTE): Single<List<ProctorSession>>
}