package org.stepik.android.presentation.course_continue.delegate

import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.InteractionSource

interface CourseContinuePresenterDelegate {
    fun continueCourse(course: Course, interactionSource: InteractionSource)
}