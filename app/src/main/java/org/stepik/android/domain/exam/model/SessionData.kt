package org.stepik.android.domain.exam.model

import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.proctor_session.model.ProctorSession

data class SessionData(
    val sectionId: Long,
    val examSession: ExamSession?,
    val proctorSession: ProctorSession?
) {
    companion object {
        val EMPTY = SessionData(0L, null, null)
    }
}
