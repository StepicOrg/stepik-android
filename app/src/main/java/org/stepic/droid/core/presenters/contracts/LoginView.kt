package org.stepic.droid.core.presenters.contracts

import com.google.android.gms.auth.api.credentials.Credential
import okhttp3.ResponseBody
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.model.Credentials

interface LoginView {

    fun onLoadingWhileLogin()

    fun onFailLogin(type: LoginFailType, credential: Credential?)

    fun onSocialLoginWithExistingEmail(email: String)

    fun onSuccessLogin(credentials : Credentials?)

    fun onRegistrationFailed(responseBody: ResponseBody?)
}
