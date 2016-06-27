package org.stepic.droid.util

import android.content.Context
import android.net.ConnectivityManager
import org.stepic.droid.base.MainApplication

object StepicUtil {
    fun isInternetAvailable(): Boolean {
        val connectivityManager = MainApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        val isConnectedOrConnecting = (activeNetworkInfo?.isConnectedOrConnecting ?: false)
        return isConnectedOrConnecting
    }
}
