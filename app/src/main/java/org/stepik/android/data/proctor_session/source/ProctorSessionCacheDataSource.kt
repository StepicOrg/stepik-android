package org.stepik.android.data.proctor_session.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.proctor_session.model.ProctorSession

interface ProctorSessionCacheDataSource {
    fun getProctorSession(id: Long): Single<ProctorSession>
    fun saveProctorSession(item: ProctorSession): Completable
}