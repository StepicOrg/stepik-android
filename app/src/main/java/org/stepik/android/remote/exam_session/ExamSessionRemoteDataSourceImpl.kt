package org.stepik.android.remote.exam_session

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.exam_session.source.ExamSessionRemoteDataSource
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.remote.exam_session.model.ExamSessionResponse
import org.stepik.android.remote.exam_session.service.ExamSessionService
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class ExamSessionRemoteDataSourceImpl
@Inject
constructor(
    private val examSessionService: ExamSessionService
) : ExamSessionRemoteDataSource {
    private val examSessionMapper = Function(ExamSessionResponse::examSessions)

    override fun getExamSession(id: Long): Single<ExamSession> =
        examSessionService
            .getExamSession(id)
            .map(examSessionMapper)
            .first()
}