package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.core.LoginFailType
import org.stepic.droid.model.AuthData

interface LoginView {

    fun onLoadingWhileLogin()

    fun onFailLogin(type: LoginFailType)

    fun onSuccessLogin(authData : AuthData?)
}
