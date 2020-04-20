package org.stepik.android.presentation.course_continue

import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course

interface CourseContinueView {
    fun showCourse(course: Course, isAdaptive: Boolean)
    fun showSteps(course: Course, lastStep: LastStep)
    fun setBlockingLoading(isLoading: Boolean)
}