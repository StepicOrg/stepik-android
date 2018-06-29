package org.stepic.droid.util

import android.content.Context
import org.stepic.droid.R
import org.stepic.droid.core.LoginFailType

fun Context.getMessageFor(type: LoginFailType): String {
    with(resources) {
        return when (type) {
            LoginFailType.connectionProblem -> getString(R.string.connectionProblems)
            LoginFailType.emailAlreadyUsed -> getString(R.string.email_already_used)
            LoginFailType.emailNotProvidedBySocial -> getString(R.string.email_not_provided_by_social)
            LoginFailType.tooManyAttempts -> getString(R.string.too_many_attempts)
            LoginFailType.emailPasswordInvalid -> getString(R.string.failLogin)
            LoginFailType.unknownError -> getString(R.string.unknown_auth_error)
        }
    }
}