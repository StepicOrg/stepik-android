package org.stepik.android.cache.proctor_session

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.proctor_session.dao.ProctorSessionDao
import org.stepik.android.data.proctor_session.source.ProctorSessionCacheDataSource
import org.stepik.android.domain.proctor_session.model.ProctorSession
import javax.inject.Inject

class ProctorSessionCacheDataSourceImpl
@Inject
constructor(
    private val proctorSessionDao: ProctorSessionDao
) : ProctorSessionCacheDataSource {
    override fun getProctorSessions(ids: List<Long>): Single<List<ProctorSession>> =
        proctorSessionDao.getProctorSessions(ids)

    override fun saveProctorSessions(items: List<ProctorSession>): Completable =
        proctorSessionDao.saveProctorSessions(items)
}