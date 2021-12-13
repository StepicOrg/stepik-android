package org.stepik.android.presentation.course.model

enum class EnrollmentError {
    NO_CONNECTION,
    FORBIDDEN,
    UNAUTHORIZED,
    COURSE_ALREADY_OWNED,
    BILLING_ERROR,
    BILLING_CANCELLED,
    BILLING_NOT_AVAILABLE,
    BILLING_NO_PURCHASES_TO_RESTORE
}