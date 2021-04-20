package org.stepik.android.data.proctor_session.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.proctor_session.source.ProctorSessionCacheDataSource
import org.stepik.android.data.proctor_session.source.ProctorSessionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.domain.proctor_session.repository.ProctorSessionRepository
import javax.inject.Inject

class ProctorSessionRepositoryImpl
@Inject
constructor(
    private val proctorSessionRemoteDataSource: ProctorSessionRemoteDataSource,
    private val proctorSessionCacheDataSource: ProctorSessionCacheDataSource
) : ProctorSessionRepository {
    private val delegate =
        ListRepositoryDelegate(
            proctorSessionRemoteDataSource::getProctorSessions,
            proctorSessionCacheDataSource::getProctorSessions,
            proctorSessionCacheDataSource::saveProctorSessions
        )
    override fun getProctorSessions(ids: List<Long>, sourceType: DataSourceType): Single<List<ProctorSession>> =
        delegate.get(ids, sourceType, allowFallback = true)
}