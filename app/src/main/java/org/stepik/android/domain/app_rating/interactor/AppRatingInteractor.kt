package org.stepik.android.domain.app_rating.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import javax.inject.Inject

class AppRatingInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {
    companion object {
        private const val MAX_RATE_TIMES = 5
        private const val MIN_SOLVED_STEPS_FOR_RATING = 5
    }

    fun needShowAppRateDialog(): Boolean =
        !sharedPreferenceHelper.wasRateHandled() &&
                isRateDelayGreater() &&
                isRateWasShownFewTimes() &&
                isUserSolveEnough()

    fun incrementSolvedStepCounter() {
        sharedPreferenceHelper.trackWhenUserSolved()
        sharedPreferenceHelper.incrementUserSolved()
    }

    fun rateHandled() {
        sharedPreferenceHelper.afterRateWasHandled()
    }

    fun rateDialogShown() {
        sharedPreferenceHelper.rateShown(DateTimeHelper.nowUtc())
    }

    private fun isRateDelayGreater(): Boolean {
        val wasShownMillis = sharedPreferenceHelper.whenRateWasShown()
        if (wasShownMillis < 0) {
            return true
        }

        val delayMillis = firebaseRemoteConfig.getLong(RemoteConfig.MIN_DELAY_RATE_DIALOG_SEC).toInt() * 1000L

        return DateTimeHelper.isBeforeNowUtc(delayMillis + wasShownMillis) // if delay is expired (before now) -> show
    }

    private fun isRateWasShownFewTimes(): Boolean {
        val wasShown = sharedPreferenceHelper.howManyRateWasShownBefore()
        return wasShown <= MAX_RATE_TIMES
    }

    private fun isUserSolveEnough(): Boolean {
        val numberOfSolved = sharedPreferenceHelper.numberOfSolved()
        return numberOfSolved >= MIN_SOLVED_STEPS_FOR_RATING
    }
}