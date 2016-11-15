package org.stepic.droid.core

import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.core.presenters.contracts.ProfileView
import org.stepic.droid.model.Profile
import org.stepic.droid.model.UserViewModel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.getFirstAndLastName
import org.stepic.droid.web.IApi
import timber.log.Timber
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

class ProfilePresenter(val threadPoolExecutor: ThreadPoolExecutor,
                       val analytic: Analytic,
                       val mainHandler: IMainHandler,
                       val api: IApi,
                       val sharedPreferences: SharedPreferenceHelper) : PresenterBase<ProfileView>() {

    var isLoading: Boolean = false //main thread only
    var userViewModel: UserViewModel? = null //both threads, but access only when isLoading = false, write isLoading = true.
    var currentStreak: Int? = null
    var maxStreak: Int? = null

    @JvmOverloads
    fun initProfile(profileId: Long = 0L) {
        if (isLoading) return
        Timber.d("initProfile")
        isLoading = true
        userViewModel?.let {
            view?.showNameImageShortBio(it)
            if (it.isMyProfile) {
                val currentStreakLocal = currentStreak
                val maxStreakLocal = maxStreak
                if (maxStreakLocal != null && currentStreakLocal != null) {
                    view?.streaksIsLoaded(currentStreakLocal, maxStreakLocal)
                    isLoading = false
                    return
                } else {
                    threadPoolExecutor.execute {
                        showStreaks(it.id)
                    }
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
            mainHandler.post {
                view?.onInternetFailed()
                isLoading = false
            }
        } else {
            val userViewModelLocal = UserViewModel(fullName = user.getFirstAndLastName(),
                    imageLink = user.avatar,
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

    @WorkerThread
    private fun showStreaks(userId: Long) {
        fun getMaxStreak(pins: ArrayList<Long>): Int {
            var maxStreak: Int = 0
            var currentStreak = 0
            pins.forEach {
                if (it != 0L) {
                    currentStreak++
                } else {
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak
                    }
                    currentStreak = 0
                }
            }
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
            return maxStreak
        }

        fun getCurrentStreak(pins: ArrayList<Long>): Int {
            var currentStreak: Int = 0
            pins.forEach {
                if (it != 0L) {
                    currentStreak++
                } else {
                    return currentStreak
                }
            }
            return currentStreak
        }

        val pins = try {
            api.getUserActivities(userId).execute().body().userActivities.firstOrNull()?.pins
        } catch (exception: Exception) {
            //if we do not have Internet or do not have access to streaks, just do nothing, because streaks is not primary on profile screen
            null
        } ?: return

        val currentStreakLocal = getCurrentStreak(pins)
        val maxStreakLocal = getMaxStreak(pins)
        mainHandler.post {
            if (currentStreak == null && maxStreak == null) {
                currentStreak = currentStreakLocal
                maxStreak = maxStreakLocal
                view?.streaksIsLoaded(currentStreak = currentStreakLocal,
                        maxStreak = maxStreakLocal)
            }
        }
    }


    @WorkerThread
    private fun showLocalProfile(profile: Profile) {
        showProfileBase(profile, isMyProfile = true)
        showStreaks(profile.id)
    }

    private fun showProfileBase(profile: Profile, isMyProfile: Boolean) {
        val userViewModelLocal = UserViewModel(fullName = profile.getFirstAndLastName(),
                imageLink = profile.avatar,
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