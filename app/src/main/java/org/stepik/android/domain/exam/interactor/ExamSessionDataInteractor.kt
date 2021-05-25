package org.stepik.android.domain.exam.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.exam.model.SessionData
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.exam_session.repository.ExamSessionRepository
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.domain.proctor_session.repository.ProctorSessionRepository
import org.stepik.android.model.Section
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class ExamSessionDataInteractor
@Inject
constructor(
    private val examSessionRepository: ExamSessionRepository,
    private val proctorSessionRepository: ProctorSessionRepository
) {
    fun getSessionData(section: Section, dataSourceType: DataSourceType): Single<SessionData> =
        getSessionData(listOf(section), dataSourceType).first()

    fun getSessionData(sections: List<Section>, dataSourceType: DataSourceType): Single<List<SessionData>> =
        Singles.zip(
            examSessionRepository.getExamSessions(sections.mapNotNull(Section::examSession), dataSourceType).onErrorReturnItem(emptyList()),
            proctorSessionRepository.getProctorSessions(sections.mapNotNull(Section::proctorSession), dataSourceType).onErrorReturnItem(emptyList())
        ) { examSessions, proctorSessions ->
            val examSessionsMap = examSessions.associateBy(ExamSession::section)
            val proctorSessionsMap = proctorSessions.associateBy(ProctorSession::section)
            sections.map { section ->
                SessionData(
                    sectionId = section.id,
                    examSession = examSessionsMap[section.id],
                    proctorSession = proctorSessionsMap[section.id]
                )
            }
        }
}