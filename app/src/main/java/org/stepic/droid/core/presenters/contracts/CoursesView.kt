package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Course

interface CoursesView {
    fun showLoading()

    fun showEmptyCourses()

    fun showConnectionProblem()

    fun showCourses(courses: List<Course>)
}
