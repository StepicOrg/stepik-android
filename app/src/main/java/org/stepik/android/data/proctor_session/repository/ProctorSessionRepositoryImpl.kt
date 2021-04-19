package org.stepik.android.data.proctor_session.repository

import io.reactivex.Single
import org.stepik.android.data.proctor_session.source.ProctorSessionCacheDataSource
import org.stepik.android.data.proctor_session.source.ProctorSessionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.domain.proctor_session.repository.ProctorSessionRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ProctorSessionRepositoryImpl
@Inject
constructor(
    private val proctorSessionRemoteDataSource: ProctorSessionRemoteDataSource,
    private val proctorSessionCacheDataSource: ProctorSessionCacheDataSource
) : ProctorSessionRepository {
    override fun getProctorSession(id: Long, sourceType: DataSourceType): Single<ProctorSession> {
        val remoteSource = proctorSessionRemoteDataSource
            .getProctorSession(id)
            .doCompletableOnSuccess(proctorSessionCacheDataSource::saveProctorSession)

        val cacheSource = proctorSessionCacheDataSource
            .getProctorSession(id)

        return when (sourceType) {
            DataSourceType.REMOTE ->
                remoteSource
                    .onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
    }
}