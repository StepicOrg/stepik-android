package org.stepic.droid.core

enum class LoginFailType {
    connectionProblem,
    tooManyAttempts,
    emailAlreadyUsed,
    emailPasswordInvalid,

    emailNotProvidedBySocial,
    unknownError
}
