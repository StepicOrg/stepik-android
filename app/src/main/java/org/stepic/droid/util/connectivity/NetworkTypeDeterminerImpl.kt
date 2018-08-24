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

        return if (activeNetwork?.isConnected == true) {
            when (activeNetwork.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.wifi
                ConnectivityManager.TYPE_MOBILE -> NetworkType.onlyMobile
                else -> NetworkType.none
            }

        } else {
            NetworkType.none
        }
    }
}
