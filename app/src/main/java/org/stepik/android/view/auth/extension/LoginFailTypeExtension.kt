package org.stepik.android.view.auth.extension

import android.content.Context
import org.stepic.droid.R
import org.stepik.android.domain.auth.model.LoginFailType

fun Context.getMessageFor(type: LoginFailType): String {
    with(resources) {
        return when (type) {
            LoginFailType.CONNECTION_PROBLEM -> getString(R.string.connectionProblems)
            LoginFailType.EMAIL_ALREADY_USED -> getString(R.string.auth_error_email_already_used)
            LoginFailType.EMAIL_NOT_PROVIDED_BY_SOCIAL -> getString(R.string.auth_error_email_not_provided_by_social)
            LoginFailType.TOO_MANY_ATTEMPTS -> getString(R.string.too_many_attempts)
            LoginFailType.EMAIL_PASSWORD_INVALID -> getString(R.string.auth_error_invalid_credentials)
            LoginFailType.UNKNOWN_ERROR -> getString(R.string.auth_error_unknown)
        }
    }
}