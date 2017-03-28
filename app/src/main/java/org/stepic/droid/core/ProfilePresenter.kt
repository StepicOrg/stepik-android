package org.stepic.droid.core

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.model.Profile
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.getFirstAndLastName
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor

class ProfilePresenter(private val threadPoolExecutor: ThreadPoolExecutor,
                       private val analytic: Analytic,
                       private val mainHandler: MainHandler,
                       private val api: Api,
                       private val sharedPreferences: SharedPreferenceHelper) : PresenterBase<ProfileView>() {

    private var isLoading: Boolean = false //main thread only
    private var userViewModel: UserViewModel? = null //both threads, but access only when isLoading = false, write isLoading = true.
    private var currentStreak: Int? = null
    private var maxStreak: Int? = null

    @JvmOverloads
    fun initProfile(profileId: Long = 0L) {
        if (isLoading) return
        isLoading = true
        userViewModel?.let {
            view?.showNameImageShortBio(it)
            if (it.isMyProfile) {
                val currentStreakLocal = currentStreak
                val maxStreakLocal = maxStreak
                if (maxStreakLocal != null && currentStreakLocal != null) {
                    view?.streaksAreLoaded(currentStreakLocal, maxStreakLocal)
                    isLoading = false
                    return
                } else {
                    threadPoolExecutor.execute {
                        showStreaks(it.id)
                    }
                    isLoading =false
                    return
                }
            } else {
                isLoading = false
                return
            }
        }

        view?.showLoadingAll()
        threadPoolExecutor.execute {
            val profile: Profile? = sharedPreferences.profile //need background thread?
            if (profileId < 0) {
                mainHandler.post {
                    view?.onProfileNotFound()
                    isLoading = false
                }
            } else if (profile != null && (profileId == 0L || profile.id == profileId)) {
                showLocalProfile(profile)
            } else {
                showInternetProfile(profileId)
            }
        }
    }

    @WorkerThread
    private fun showInternetProfile(userId: Long) {
        //1) show profile
        //2) no internet
        //3) user hide profile == Anonymous. We do not need handle this sitation

        val user = try {
            api.getUsers(longArrayOf(userId)).execute().body().users.firstOrNull()
        } catch (exception: Exception) {
            null
        }

        if (user == null) {
            analytic.reportEvent(Analytic.Profile.OPEN_NO_INTERNET)
            mainHandler.post {
                view?.onInternetFailed()
                isLoading = false
            }
        } else {
            val userViewModelLocal = UserViewModel(fullName = user.getFirstAndLastName(),
                    imageLink = user.getAvatarPath(),
                    shortBio = stringOrEmpty(user.short_bio),
                    information = stringOrEmpty((user.details)),
                    isMyProfile = false,
                    id = userId)
            this.userViewModel = userViewModelLocal

            mainHandler.post {
                view?.showNameImageShortBio(userViewModelLocal)
                isLoading = false
            }
        }

    }

    fun showStreakForStoredUser() {
        threadPoolExecutor.execute {
            sharedPreferences.profile?.id?.let {
                showStreaks(it)
            }
        }
    }

    @WorkerThread
    private fun showStreaks(userId: Long) {
        val pins = try {
            api.getUserActivities(userId).execute().body().userActivities.firstOrNull()?.pins
        } catch (exception: Exception) {
            //if we do not have Internet or do not have access to streaks, just do nothing, because streaks is not primary on profile screen
            analytic.reportEvent(Analytic.Profile.STREAK_NO_INTERNET)
            null
        } ?: return

        val currentStreakLocal = StepikUtil.getCurrentStreak(pins)
        val maxStreakLocal = StepikUtil.getMaxStreak(pins)
        mainHandler.post {
            currentStreak = currentStreakLocal
            maxStreak = maxStreakLocal
            view?.streaksAreLoaded(currentStreak = currentStreakLocal,
                    maxStreak = maxStreakLocal)
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
                id = profile.id)
        this.userViewModel = userViewModelLocal

        mainHandler.post {
            view?.showNameImageShortBio(userViewModelLocal)
            isLoading = false
        }
    }

    private fun stringOrEmpty(str: String?): String {
        val source = str ?: ""
        if (source.isBlank()) {
            return ""
        } else {
            return source
        }
    }

}