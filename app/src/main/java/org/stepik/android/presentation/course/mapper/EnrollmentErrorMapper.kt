package org.stepik.android.presentation.course.mapper

import org.solovyev.android.checkout.BillingException
import org.solovyev.android.checkout.ResponseCodes
import org.stepik.android.domain.course_payments.exception.CoursePurchaseVerificationException
import org.stepik.android.presentation.course.model.EnrollmentError
import retrofit2.HttpException
import java.net.HttpURLConnection

fun Throwable.toEnrollmentError(): EnrollmentError =
    when(this) {
        is HttpException ->
            when(code()) {
                HttpURLConnection.HTTP_FORBIDDEN ->
                    EnrollmentError.FORBIDDEN

                HttpURLConnection.HTTP_UNAUTHORIZED ->
                    EnrollmentError.UNAUTHORIZED

                HttpURLConnection.HTTP_BAD_REQUEST ->
                    EnrollmentError.SERVER_ERROR

                else ->
                    EnrollmentError.NO_CONNECTION
            }

        is BillingException ->
            when(response) {
                ResponseCodes.USER_CANCELED ->
                    EnrollmentError.BILLING_CANCELLED

                ResponseCodes.BILLING_UNAVAILABLE ->
                    EnrollmentError.BILLING_NOT_AVAILABLE

                else ->
                    EnrollmentError.BILLING_ERROR
            }

        is CoursePurchaseVerificationException ->
            EnrollmentError.SERVER_ERROR

        else ->
            EnrollmentError.NO_CONNECTION
    }