package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.di.mainscreen.MainScreenScope
import org.stepic.droid.model.Profile
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@MainScreenScope
class ProfileMainFeedPresenter
@Inject constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        analytic: Analytic,
        private val stepikLogoutManager: StepikLogoutManager) : PresenterWithPotentialLeak<ProfileMainFeedView>(analytic) {

    val isProfileFetching = AtomicBoolean(false)
    var profile: Profile? = null

    fun fetchProfile() {
        if (!isProfileFetching.compareAndSet(false, true))
            return

        profile?.let {
            view?.showProfile(it)
            isProfileFetching.set(false)
            return
        }

        //we do not have profile in  -> restore it from preferences or Internet
        threadPoolExecutor.execute {
            try {
                if (sharedPreferenceHelper.authResponseFromStore == null) {
                    mainHandler.post {
                        view?.showAnonymous()
                    }
                    return@execute
                }


                val cachedProfile: Profile? = sharedPreferenceHelper.profile
                if (cachedProfile != null) {
                    profile = cachedProfile
                    mainHandler.post {
                        view?.showProfile(cachedProfile) // instant update
                    }
                }

                //after that try to update profile, because user can change avatar or something at web.
                try {
                    val tempProfile = api.userProfile.execute().body().profile!!
                    val emailIds = tempProfile.emailAddresses
                    if (emailIds != null && emailIds.isNotEmpty()) {
                        try {
                            api.getEmailAddresses(emailIds).execute().body()?.emailAddresses?.let {
                                if (it.isNotEmpty()) {
                                    sharedPreferenceHelper.storeEmailAddresses(it)
                                }
                            }
                        } catch (exceptionEmails: Exception) {
                            //ok emails is not critical
                        }

                    }
                    sharedPreferenceHelper.storeProfile(tempProfile) //FIXME in some cases it can be after logout.
                    profile = tempProfile
                    mainHandler.post { view?.showProfile(tempProfile) }
                } catch (exception: Exception) {
                    //no internet for loading profile
                }
            } finally {
                isProfileFetching.set(false)
            }
        }
    }

    fun logout() {
        view?.showLogoutLoading()
        profile = null
        view?.showAnonymous()
        analytic.reportEvent(org.stepic.droid.analytic.Analytic.Interaction.CLICK_YES_LOGOUT)
        stepikLogoutManager.logout {
            view?.onLogoutSuccess()
        }
    }

}
