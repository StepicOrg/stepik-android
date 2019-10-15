package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.di.mainscreen.MainScreenScope
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import org.stepik.android.model.user.Profile
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

    private val isProfileFetching = AtomicBoolean(false)
    private var profile: Profile? = null

    fun fetchProfile() {
        if (!isProfileFetching.compareAndSet(false, true)) {
            return
        }

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
                    val tempProfile = api.userProfile.execute().body()?.getProfile() ?: throw IllegalStateException("profile can't be null")
                    val emailIds = tempProfile.emailAddresses
                    if (emailIds?.isNotEmpty() == true) {
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
                    sharedPreferenceHelper.storeProfile(tempProfile)
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
        analytic.reportEvent(Analytic.Interaction.CLICK_YES_LOGOUT)
        stepikLogoutManager.logout {
            view?.onLogoutSuccess()
        }
    }

}
