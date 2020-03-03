package org.stepik.android.presentation.course_continue.delegate

import org.stepik.android.model.Course

interface ContinueCoursePresenterDelegate {
    fun continueCourse(course: Course)
}