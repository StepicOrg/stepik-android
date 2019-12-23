package org.stepic.droid.util

import android.content.Context
import android.net.ConnectivityManager
import org.stepic.droid.base.App
import org.stepic.droid.model.CurrentStreakExtended

object StepikUtil {
    fun isInternetAvailable(): Boolean {
        val connectivityManager = App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        val isConnectedOrConnecting = (activeNetworkInfo?.isConnectedOrConnecting ?: false)
        return isConnectedOrConnecting
    }

    /**
     * *Positive* current streak, which is not zero in the start of day and increased if today user solve some tasks.
     */
    fun getCurrentStreak(pins: List<Long>): Int {
        return getCurrentStreakExtended(pins).currentStreak
    }

    fun getCurrentStreakExtended(pins: List<Long>): CurrentStreakExtended {
        val today = if (pins[0] == 0L) {
            0
        } else {
            1
        }
        var currentStreak: Int = 0
        for (i in 1 until pins.size) {
            if (pins[i] != 0L) {
                currentStreak++
            } else {
                return CurrentStreakExtended(currentStreak + today, today > 0)
            }
        }
        return CurrentStreakExtended(currentStreak + today, today > 0)
    }
}
