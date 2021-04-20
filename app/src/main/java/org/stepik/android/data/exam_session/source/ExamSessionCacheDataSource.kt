package org.stepik.android.data.exam_session.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.exam_session.model.ExamSession

interface ExamSessionCacheDataSource {
    fun getExamSessions(ids: List<Long>): Single<List<ExamSession>>
    fun saveExamSessions(items: List<ExamSession>): Completable
}