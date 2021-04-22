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
    override fun getExamSessions(ids: List<Long>): Single<List<ExamSession>> =
        examSessionDao.getExamSessions(ids)

    override fun saveExamSessions(items: List<ExamSession>): Completable =
        examSessionDao.saveExamSessions(items)
}