package org.stepic.droid.util

import android.content.Context
import org.stepic.droid.R
import org.stepic.droid.core.LoginFailType

fun Context.getMessageFor(type: LoginFailType): String {
    with(resources) {
        return when (type) {
            LoginFailType.connectionProblem -> getString(R.string.connectionProblems)
            LoginFailType.emailAlreadyUsed -> getString(R.string.email_already_used)
            LoginFailType.tooManyAttempts -> getString(R.string.too_many_attempts)
            LoginFailType.emailPasswordInvalid -> getString(R.string.failLogin)
        }
    }
}