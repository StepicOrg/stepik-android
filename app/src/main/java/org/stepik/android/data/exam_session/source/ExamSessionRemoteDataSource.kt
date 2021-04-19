package org.stepik.android.data.exam_session.source

import io.reactivex.Single
import org.stepik.android.domain.exam_session.model.ExamSession

interface ExamSessionRemoteDataSource {
    fun getExamSession(id: Long): Single<ExamSession>
}