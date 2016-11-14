package org.stepic.droid.core

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.model.Profile
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.getFirstAndLastName
import org.stepic.droid.web.IApi
import java.util.concurrent.ThreadPoolExecutor

class ProfilePresenter(val threadPoolExecutor: ThreadPoolExecutor,
                       val analytic: Analytic,
                       val mainHandler: IMainHandler,
                       val api: IApi,
                       val sharedPreferences: SharedPreferenceHelper) : PresenterBase<ProfileView>() {

    @JvmOverloads
    fun initProfile(profileId: Long = -1L) {
        //todo handle rotates

        threadPoolExecutor.execute {
            val profile: Profile? = sharedPreferences.profile //need background thread?
            if (profile != null && (profileId == -1L || profile.id == profileId)) {
                showLocalProfile(profile)
            } else {

            }
        }


        view?.showNameImageShortBio(fullName = "Kirill Makarov",
                imageLink = "https://stepik.org/media/users/avatar/1718803?1443605961",
                shortBio = "Android Expert",
                isMyProfile = true)

        view?.streaksIsLoaded(currentStreak = 0, maxStreak = 3)
    }


    @WorkerThread
    private fun showLocalProfile(profile: Profile) {
        val fullName = profile.getFirstAndLastName()
        val imageLink = profile.avatar

        val shortBioSource = profile.short_bio ?: ""
        val shortBio: String =
                if (shortBioSource.isBlank()) {
                    ""
                } else {
                    shortBioSource
                }

        mainHandler.post {
            view?.showNameImageShortBio(fullName, imageLink, shortBio, isMyProfile = true)
        }
    }

}