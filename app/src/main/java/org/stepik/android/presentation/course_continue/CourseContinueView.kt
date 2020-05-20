package org.stepik.android.presentation.course_continue

import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course

interface CourseContinueView {
    fun showCourse(course: Course, source: CourseViewSource, isAdaptive: Boolean)
    fun showSteps(course: Course, source: CourseViewSource, lastStep: LastStep)
    fun setBlockingLoading(isLoading: Boolean)
}