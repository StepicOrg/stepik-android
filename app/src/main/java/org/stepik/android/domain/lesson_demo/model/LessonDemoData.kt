package org.stepik.android.domain.lesson_demo.model

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult

data class LessonDemoData(
    val deeplinkPromoCode: DeeplinkPromoCode,
    val coursePurchaseDataResult: CoursePurchaseDataResult
)
