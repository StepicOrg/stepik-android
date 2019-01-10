package org.stepik.android.presentation.course.mapper

import org.stepik.android.presentation.course.model.EnrollmentError
import retrofit2.HttpException
import java.net.HttpURLConnection

fun Throwable.toEnrollmentError(): EnrollmentError =
    when((this as? HttpException)?.code()) {
        HttpURLConnection.HTTP_FORBIDDEN ->
            EnrollmentError.FORBIDDEN

        HttpURLConnection.HTTP_UNAUTHORIZED ->
            EnrollmentError.UNAUTHORIZED

        else ->
            EnrollmentError.NO_CONNECTION
    }