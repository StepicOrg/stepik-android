package org.stepik.android.domain.exam.resolver

import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.exam.model.ExamStatus
import org.stepik.android.domain.exam_session.model.ExamSession
import org.stepik.android.domain.proctor_session.model.ProctorSession
import org.stepik.android.model.Section
import javax.inject.Inject

class ExamStatusResolver
@Inject
constructor() {
    fun resolveExamStatus(section: Section, examSession: ExamSession?, proctorSession: ProctorSession?): ExamStatus =
        when {
            isExamCanStart(section, examSession, proctorSession) -> ExamStatus.IS_CAN_START
            isExamActive(examSession, proctorSession) -> ExamStatus.IN_PROGRESS
            isExamFinished(section, examSession, proctorSession) -> ExamStatus.FINISHED
            else -> ExamStatus.CANNOT_START
        }

    private fun isExamCanStart(section: Section, examSession: ExamSession?, proctorSession: ProctorSession?): Boolean {
        if (!section.isExam) {
            return false
        }

        val isReachable = (section.isActive || section.actions?.testSection != null) && (section.progress != null || section.isExam)
        if (!isReachable) {
            return false
        }

        if (examSession != null) {
            return false
        }

        if (proctorSession?.isFinished == true) {
            return false
        }

        if (section.actions?.testSection != null) {
            return false
        }

        val isExamTime = (section.beginDate == null || (section.beginDate?.time!! < DateTimeHelper.nowUtc()) && (section.endDate == null || (DateTimeHelper.nowUtc() < section.endDate?.time!!)))
        val isRequirementSatisfied = section.isRequirementSatisfied
        return isExamTime && isRequirementSatisfied
    }

    private fun isExamActive(examSession: ExamSession?, proctorSession: ProctorSession?): Boolean =
        (examSession?.isActive ?: false) && !(proctorSession?.isFinished ?: false)

    private fun isExamFinished(section: Section, examSession: ExamSession?, proctorSession: ProctorSession?): Boolean {
        if (isExamCanStart(section, examSession, proctorSession) || isExamActive(examSession, proctorSession)) {
            return false
        }

        val flag = section.endDate?.let { it.time < DateTimeHelper.nowUtc() } ?: false

        return flag || (proctorSession?.isFinished ?: false) || examSession?.id != null
    }
}