package org.stepik.android.domain.exam_session.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.exam_session.model.ExamSession

interface ExamSessionRepository {
    fun getExamSessions(ids: List<Long>, sourceType: DataSourceType = DataSourceType.REMOTE): Single<List<ExamSession>>
}