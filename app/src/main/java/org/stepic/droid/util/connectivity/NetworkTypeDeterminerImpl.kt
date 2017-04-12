package org.stepic.droid.util.connectivity

import android.net.ConnectivityManager
import android.net.NetworkInfo
import javax.inject.Inject

class NetworkTypeDeterminerImpl
@Inject constructor(
        private val connectivityManager: ConnectivityManager
) : NetworkTypeDeterminer {

    override fun determineNetworkType(): NetworkType {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (activeNetwork?.isConnected ?: false) {
            val networkType = activeNetwork?.type

            return when (networkType) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.wifi
                ConnectivityManager.TYPE_MOBILE -> NetworkType.onlyMobile
                else -> NetworkType.none
            }

        } else {
            return NetworkType.none
        }
    }
}
