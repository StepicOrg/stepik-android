package org.stepik.android.data.exam_session.repository

import io.reactivex.Single
import org.stepik.android.data.exam_session.source.ExamSessionCacheDataSource
import org.stepik.android.data.exam_session.source.ExamSessionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.exam_session.repository.ExamSessionRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ExamSessionRepositoryImpl
@Inject
constructor(
    private val examSessionRemoteDataSource: ExamSessionRemoteDataSource,
    private val examSessionCacheDataSource: ExamSessionCacheDataSource
) : ExamSessionRepository {
    override fun getExamSession(id: Long, sourceType: DataSourceType): Single<ExamSession> {
        val remoteSource = examSessionRemoteDataSource
            .getExamSession(id)
            .doCompletableOnSuccess(examSessionCacheDataSource::saveExamSession)

        val cacheSource = examSessionCacheDataSource
            .getExamSession(id)

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