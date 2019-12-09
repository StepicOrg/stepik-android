package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.ProfileMainFeedView
import org.stepic.droid.di.mainscreen.MainScreenScope
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.email_address.repository.EmailAddressRepository
import org.stepik.android.domain.user_profile.repository.UserProfileRepository
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@MainScreenScope
class ProfileMainFeedPresenter
@Inject constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val emailAddressRepository: EmailAddressRepository,
    private val userProfileRepository: UserProfileRepository,
    private val threadPoolExecutor: ThreadPoolExecutor,
    analytic: Analytic
) : PresenterWithPotentialLeak<ProfileMainFeedView>(analytic) {

    private val isProfileFetching = AtomicBoolean(false)

    fun fetchProfile() {
        if (!isProfileFetching.compareAndSet(false, true)) {
            return
        }
        threadPoolExecutor.execute {
            try {
                val tempProfile = userProfileRepository.getUserProfile().blockingGet()?.second
                    ?: throw IllegalStateException("profile can't be null")
                val emailIds = tempProfile.emailAddresses
                if (emailIds?.isNotEmpty() == true) {
                    try {
                        emailAddressRepository.getEmailAddresses(*emailIds).blockingGet().let {
                            if (it.isNotEmpty()) {
                                sharedPreferenceHelper.storeEmailAddresses(it)
                            }
                        }
                    } catch (exceptionEmails: Exception) {
                        //ok emails is not critical
                    }
                }
                sharedPreferenceHelper.storeProfile(tempProfile)
            } catch (exception: Exception) {
                //no internet for loading profile
            } finally {
                isProfileFetching.set(false)
            }
        }
    }
}
