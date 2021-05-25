package org.stepik.android.domain.course_payments.mapper

import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.model.Course
import javax.inject.Inject

class DefaultPromoCodeMapper
@Inject
constructor() {
    fun mapToDefaultPromoCode(course: Course): DefaultPromoCode =
        if (course.defaultPromoCodeName != null &&
            course.defaultPromoCodePrice != null &&
            course.defaultPromoCodeDiscount != null
        ) {
            DefaultPromoCode(
                course.defaultPromoCodeName!!,
                course.defaultPromoCodePrice!!,
                course.defaultPromoCodeDiscount!!,
                course.defaultPromoCodeExpireDate
            )
        } else {
            DefaultPromoCode.EMPTY
        }
}