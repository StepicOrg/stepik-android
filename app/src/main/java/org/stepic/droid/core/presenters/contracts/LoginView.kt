package org.stepic.droid.core.presenters.contracts

import com.google.android.gms.auth.api.credentials.Credential
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.model.AuthData

interface LoginView {

    fun onLoadingWhileLogin()

    fun onFailLogin(type: LoginFailType, credential: Credential?)

    fun onSuccessLogin(authData : AuthData?)
}
