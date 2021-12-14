package org.stepik.android.presentation.course_purchase.model

sealed class CoursePurchaseDataResult {
    object Empty : CoursePurchaseDataResult()
    data class Result(val coursePurchaseData: CoursePurchaseData) : CoursePurchaseDataResult()
}
