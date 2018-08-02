package org.stepic.droid.persistence.model

import android.app.DownloadManager
import java.util.*

class DownloadConfiguration(
        val allowedNetworkTypes: EnumSet<NetworkType>,
        val videoQuality: String
) {
    enum class NetworkType(val systemNetworkType: Int) {
        MOBILE(DownloadManager.Request.NETWORK_MOBILE),
        WIFI(DownloadManager.Request.NETWORK_WIFI)
    }
}