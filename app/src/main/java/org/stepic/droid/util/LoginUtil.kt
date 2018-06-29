package org.stepic.droid.util

import android.content.Context
import org.stepic.droid.R
import org.stepic.droid.core.LoginFailType

fun Context.getMessageFor(type: LoginFailType): String {
    with(resources) {
        return when (type) {
            LoginFailType.CONNECTION_PROBLEM -> getString(R.string.connectionProblems)
            LoginFailType.EMAIL_ALREADY_USED -> getString(R.string.email_already_used)
            LoginFailType.EMAIL_NOT_PROVIDED_BY_SOCIAL -> getString(R.string.email_not_provided_by_social)
            LoginFailType.TOO_MANY_ATTEMPTS -> getString(R.string.too_many_attempts)
            LoginFailType.EMAIL_PASSWORD_INVALID -> getString(R.string.failLogin)
            LoginFailType.UNKNOWN_ERROR -> getString(R.string.unknown_auth_error)
        }
    }
}