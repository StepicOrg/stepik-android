package org.stepik.android.view.course.resolver

import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.model.Course
import org.stepik.android.view.course.model.CoursePromoCodeInfo
import javax.inject.Inject

class CoursePromoCodeResolver
@Inject
constructor() {
    fun resolvePromoCodeInfo(deeplinkPromoCode: DeeplinkPromoCode, defaultPromoCode: DefaultPromoCode, course: Course): CoursePromoCodeInfo =
        when {
            deeplinkPromoCode != DeeplinkPromoCode.EMPTY ->
                CoursePromoCodeInfo(deeplinkPromoCode.currencyCode, deeplinkPromoCode.price, true)

            defaultPromoCode != DefaultPromoCode.EMPTY &&
                    (defaultPromoCode.defaultPromoCodeExpireDate == null || defaultPromoCode.defaultPromoCodeExpireDate.time > DateTimeHelper.nowUtc()) && course.currencyCode != null ->
                CoursePromoCodeInfo(course.currencyCode!!, defaultPromoCode.defaultPromoCodePrice, true)

            else ->
                CoursePromoCodeInfo("", "", false)
        }
}