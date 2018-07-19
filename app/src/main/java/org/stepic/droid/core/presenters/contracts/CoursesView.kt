package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Course

interface CoursesView {
    fun showLoading()

    fun showEmptyCourses()

    fun showConnectionProblem()

    fun showCourses(courses: List<Course>)
}
