package org.stepic.droid.util

import android.content.Context
import android.net.ConnectivityManager
import org.stepic.droid.base.MainApplication

object StepikUtil {
    fun isInternetAvailable(): Boolean {
        val connectivityManager = MainApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        val isConnectedOrConnecting = (activeNetworkInfo?.isConnectedOrConnecting ?: false)
        return isConnectedOrConnecting
    }

    fun getMaxStreak(pins: List<Long>): Int {
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

    /**
     * *Positive* current streak, which is not zero in the start of day and increased if today user solve some tasks.
     */
    fun getCurrentStreak(pins: List<Long>): Int {
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
                return currentStreak + today
            }
        }
        return currentStreak
    }
}
