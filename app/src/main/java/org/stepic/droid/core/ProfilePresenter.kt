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
import java.util.*
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
    }


    @WorkerThread
    private fun showLocalProfile(profile: Profile) {
        fun showStreaks(userId: Long) {
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
                        currentStreak++;
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

            val currentStreak = getCurrentStreak(pins)
            val maxStreak = getMaxStreak(pins)
            mainHandler.post {
                view?.streaksIsLoaded(currentStreak = currentStreak,
                        maxStreak = maxStreak)
            }
        }

        showProfileBase(profile, isMyProfile = true)
        showStreaks(profile.id)
    }

    private fun showProfileBase(profile: Profile, isMyProfile: Boolean) {
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
            view?.showNameImageShortBio(fullName, imageLink, shortBio, isMyProfile)
        }
    }

}