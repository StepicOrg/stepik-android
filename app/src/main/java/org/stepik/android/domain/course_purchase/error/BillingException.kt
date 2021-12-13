package org.stepik.android.domain.course_purchase.error

data class BillingException(val responseCode: Int) : Exception()