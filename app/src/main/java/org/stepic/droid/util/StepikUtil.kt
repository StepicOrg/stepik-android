package org.stepic.droid.util

import android.content.Context
import android.net.ConnectivityManager
import org.stepic.droid.base.MainApplication
import org.stepic.droid.model.CurrentMaxStreak
import java.util.*

object StepikUtil {
    fun isInternetAvailable(): Boolean {
        val connectivityManager = MainApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        val isConnectedOrConnecting = (activeNetworkInfo?.isConnectedOrConnecting ?: false)
        return isConnectedOrConnecting
    }

    fun getCurrentAndMaxStreak(pins: ArrayList<Long>): CurrentMaxStreak {
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

        val currentStreak = getCurrentStreak(pins)
        val maxStreak = getMaxStreak(pins)

        return CurrentMaxStreak(currentStreak, maxStreak)
    }
}
