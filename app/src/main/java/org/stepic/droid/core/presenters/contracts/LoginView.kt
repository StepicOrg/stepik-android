package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.core.LoginFailType

interface LoginView {

    fun onLoadingWhileLogin()

    fun onFailLogin(type: LoginFailType)

    fun onSuccessLogin()
}
