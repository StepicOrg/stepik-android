package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Course

interface CoursesView {
    fun showLoading()

    fun showEmptyCourses()

    fun showConnectionProblem()

    fun showCourses(courses: MutableList<Course>)
}
