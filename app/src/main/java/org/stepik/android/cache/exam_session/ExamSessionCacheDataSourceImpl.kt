package org.stepik.android.cache.exam_session

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.exam_session.dao.ExamSessionDao
import org.stepik.android.data.exam_session.source.ExamSessionCacheDataSource
import org.stepik.android.domain.exam_session.model.ExamSession
import javax.inject.Inject

class ExamSessionCacheDataSourceImpl
@Inject
constructor(
    private val examSessionDao: ExamSessionDao
) : ExamSessionCacheDataSource {
    override fun getExamSession(id: Long): Single<ExamSession> =
        examSessionDao.getExamSessions(id)

    override fun saveExamSession(item: ExamSession): Completable =
        examSessionDao.saveExamSessions(item)
}