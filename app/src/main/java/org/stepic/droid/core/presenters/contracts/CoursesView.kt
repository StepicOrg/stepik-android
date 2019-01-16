package org.stepic.droid.core.presenters.contracts

import org.solovyev.android.checkout.Sku
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.model.Course

interface CoursesView {
    fun showLoading()

    fun showEmptyCourses()

    fun showConnectionProblem()

    fun showCourses(courses: List<Course>, skus: Map<String, Sku>, coursePayments: Map<Long, CoursePayment>)
}
