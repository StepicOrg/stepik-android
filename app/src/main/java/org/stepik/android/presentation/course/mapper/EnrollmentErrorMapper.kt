package org.stepik.android.presentation.course.mapper

import com.android.billingclient.api.BillingClient
import org.stepik.android.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.domain.course_payments.exception.CourseAlreadyOwnedException
import org.stepik.android.domain.course_purchase.error.BillingException
import org.stepik.android.presentation.course.model.EnrollmentError
import retrofit2.HttpException
import java.net.HttpURLConnection

fun Throwable.toEnrollmentError(): EnrollmentError =
    when (this) {
        is HttpException ->
            when (code()) {
                HttpURLConnection.HTTP_FORBIDDEN ->
                    EnrollmentError.FORBIDDEN

                HttpURLConnection.HTTP_UNAUTHORIZED ->
                    EnrollmentError.UNAUTHORIZED

                else ->
                    EnrollmentError.NO_CONNECTION
            }

        is BillingException ->
            when (responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED ->
                    EnrollmentError.BILLING_CANCELLED

                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                    EnrollmentError.BILLING_NOT_AVAILABLE

                else ->
                    EnrollmentError.BILLING_ERROR
            }

        is CourseAlreadyOwnedException ->
            EnrollmentError.COURSE_ALREADY_OWNED

        is NoPurchasesToRestoreException ->
            EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE

        else ->
            EnrollmentError.NO_CONNECTION
    }