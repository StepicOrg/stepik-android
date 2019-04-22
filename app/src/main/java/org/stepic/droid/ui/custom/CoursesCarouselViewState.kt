package org.stepic.droid.ui.custom

import org.solovyev.android.checkout.Sku
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.model.Course

data class CoursesCarouselViewState(
    val courses: List<Course>,
    val skus: Map<String, Sku>,
    val coursePayments: Map<Long, CoursePayment>,
    val scrollPosition: Int
)