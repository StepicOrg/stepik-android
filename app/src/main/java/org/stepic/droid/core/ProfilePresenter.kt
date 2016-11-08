package org.stepic.droid.core

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.IApi
import java.util.concurrent.ThreadPoolExecutor

class ProfilePresenter(val threadPoolExecutor: ThreadPoolExecutor,
                       val analytic: Analytic,
                       val mainHandler: IMainHandler,
                       val api: IApi,
                       val sharedPreferences: SharedPreferenceHelper) : PresenterBase<ProfileView>() {

    fun initProfile() {
        //todo handle rotates
        view?.showNameImageShortBio(fullName = "Kirill Makarov",
                imageLink = "https://stepik.org/media/users/avatar/1718803?1443605961",
                shortBio = "Android Expert",
                isMyProfile = true)
    }

}