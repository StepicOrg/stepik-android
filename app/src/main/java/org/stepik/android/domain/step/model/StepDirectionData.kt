package org.stepik.android.domain.step.model

import org.stepik.android.domain.exam.model.SessionData
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.course_content.model.RequiredSection

data class StepDirectionData(
    val lessonData: LessonData,
    val requiredSection: RequiredSection?,
    val examSessionData: SessionData?
)
