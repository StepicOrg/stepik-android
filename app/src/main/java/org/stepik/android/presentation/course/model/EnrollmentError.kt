package org.stepik.android.presentation.course.model

enum class EnrollmentError {
    NO_CONNECTION,
    FORBIDDEN,
    UNAUTHORIZED,
    SERVER_ERROR,
    BILLING_ERROR,
    BILLING_CANCELLED,
    BILLING_NOT_AVAILABLE
}