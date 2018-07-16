package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.ProfilePresenter
import org.stepic.droid.model.Profile
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.getFirstAndLastName
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class ProfilePresenterImpl
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        analytic: Analytic,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val sharedPreferences: SharedPreferenceHelper
) : ProfilePresenter(analytic) {

    private var isLoading: Boolean = false //main thread only
    private var userViewModel: UserViewModel? = null //both threads, but access only when isLoading = false, write isLoading = true.
    private var currentStreak: Int? = null
    private var maxStreak: Int? = null
    private var haveSolvedToday: Boolean? = null

    override fun initProfile() {
        // default params are not allowed for override.
        // moreover, abstract function with default param is used in Java code
        initProfile(profileId = 0)
    }

    override fun initProfile(profileId: Long) {
        if (isLoading) return
        isLoading = true
        userViewModel?.let {
            view?.showNameImageShortBio(it)
            if (it.isMyProfile) {
                val currentStreakLocal = currentStreak
                val maxStreakLocal = maxStreak
                val haveSolvedTodayLocal = haveSolvedToday
                if (maxStreakLocal != null && currentStreakLocal != null && haveSolvedTodayLocal != null) {
                    view?.streaksAreLoaded(currentStreakLocal, maxStreakLocal, haveSolvedTodayLocal)
                    isLoading = false
                    return
                } else {
                    threadPoolExecutor.execute {
                        showStreaks(it.id)
                    }
                    isLoading = false
                    return
                }
            } else {
                isLoading = false
                return
            }
        }

        view?.showLoadingAll()
        threadPoolExecutor.execute {
            val profile: Profile? = sharedPreferences.profile
            if (profileId < 0) {
                mainHandler.post {
                    view?.onProfileNotFound()
                    isLoading = false
                }
            } else if (profile != null && (profileId == 0L || profile.id == profileId) && !profile.is_guest) {
                showLocalProfile(profile)
            } else if (profileId == 0L && (profile != null && profile.is_guest || profile == null)) {
                try {
                    val realProfile = api.userProfile.execute().body()?.getProfile() ?: throw IllegalStateException("profile can't be null on API here")
                    sharedPreferences.storeProfile(realProfile)
                    showLocalProfile(realProfile)
                } catch (noInternetOrPermission: Exception) {
                    mainHandler.post {
                        view?.onInternetFailed()
                        isLoading = false
                    }
                }
            } else {
                showInternetProfile(profileId)
            }
        }
    }

    @WorkerThread
    private fun showInternetProfile(userId: Long) {
        //1) show profile
        //2) no internet
        //3) user hide profile == Anonymous. We do not need handle this situation

        val user = try {
            api.getUsers(longArrayOf(userId)).execute().body()?.users?.firstOrNull()
        } catch (exception: Exception) {
            null
        }

        if (user == null) {
            mainHandler.post {
                view?.onInternetFailed()
                isLoading = false
            }
        } else {
            val userViewModelLocal = UserViewModel(fullName = user.fullName ?: "",
                    imageLink = user.avatar,
                    shortBio = stringOrEmpty(user.shortBio),
                    information = stringOrEmpty((user.details)),
                    isMyProfile = false,
                    isPrivate = user.isPrivate,
                    id = userId)
            this.userViewModel = userViewModelLocal

            mainHandler.post {
                view?.showNameImageShortBio(userViewModelLocal)
                isLoading = false
            }
        }

    }

    override fun showStreakForStoredUser() {
        threadPoolExecutor.execute {
            sharedPreferences.profile?.id?.let {
                showStreaks(it)
            }
        }
    }

    @WorkerThread
    private fun showStreaks(userId: Long) {
        val pins = try {
            api.getUserActivities(userId).execute().body()?.userActivities?.firstOrNull()?.pins
        } catch (exception: Exception) {
            //if we do not have Internet or do not have access to streaks, just do nothing, because streaks is not primary on profile screen
            null
        } ?: return

        val currentStreakLocal = StepikUtil.getCurrentStreak(pins)
        val maxStreakLocal = StepikUtil.getMaxStreak(pins)
        val haveSolvedTodayLocal = pins.first() != 0L
        mainHandler.post {
            haveSolvedToday = haveSolvedTodayLocal
            currentStreak = currentStreakLocal
            maxStreak = maxStreakLocal
            view?.streaksAreLoaded(currentStreak = currentStreakLocal,
                    maxStreak = maxStreakLocal,
                    haveSolvedToday = haveSolvedTodayLocal)
        }
    }

    @WorkerThread
    private fun showLocalProfile(profile: Profile) {
        analytic.reportEvent(Analytic.Profile.SHOW_LOCAL)
        showProfileBase(profile, isMyProfile = true)
        showStreaks(profile.id)
    }

    private fun showProfileBase(profile: Profile, isMyProfile: Boolean) {
        val userViewModelLocal = UserViewModel(fullName = profile.getFirstAndLastName(),
                imageLink = profile.getAvatarPath(),
                shortBio = stringOrEmpty(profile.short_bio),
                information = stringOrEmpty((profile.details)),
                isMyProfile = isMyProfile,
                isPrivate = profile.is_private ?: false,
                id = profile.id)
        this.userViewModel = userViewModelLocal


        mainHandler.post {
            if (profile.is_guest) {
                view?.onUserNotAuth()
            } else {
                view?.showNameImageShortBio(userViewModelLocal)
            }
            isLoading = false
        }
    }

    private fun stringOrEmpty(str: String?): String {
        val source = str ?: ""
        return if (source.isBlank()) "" else source
    }

}