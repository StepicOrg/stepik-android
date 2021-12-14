package org.stepik.android.domain.lesson_demo.model

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

data class LessonDemoData(
    val deeplinkPromoCode: DeeplinkPromoCode,
    val coursePurchaseData: CoursePurchaseData?
)
