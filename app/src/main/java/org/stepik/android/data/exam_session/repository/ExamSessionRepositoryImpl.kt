package org.stepik.android.data.exam_session.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.exam_session.source.ExamSessionCacheDataSource
import org.stepik.android.data.exam_session.source.ExamSessionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.exam_session.repository.ExamSessionRepository
import javax.inject.Inject

class ExamSessionRepositoryImpl
@Inject
constructor(
    private val examSessionRemoteDataSource: ExamSessionRemoteDataSource,
    private val examSessionCacheDataSource: ExamSessionCacheDataSource
) : ExamSessionRepository {
    private val delegate =
        ListRepositoryDelegate(
            examSessionRemoteDataSource::getExamSessions,
            examSessionCacheDataSource::getExamSessions,
            examSessionCacheDataSource::saveExamSessions
        )
    override fun getExamSessions(ids: List<Long>, sourceType: DataSourceType): Single<List<ExamSession>> =
        delegate.get(ids, sourceType, allowFallback = true)
}