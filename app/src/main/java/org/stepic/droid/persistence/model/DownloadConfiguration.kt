package org.stepic.droid.persistence.model

import android.app.DownloadManager
import java.util.*

data class DownloadConfiguration(
    val allowedNetworkTypes: EnumSet<NetworkType> = EnumSet.noneOf(NetworkType::class.java),
    val videoQuality: String
) {
    enum class NetworkType(val systemNetworkType: Int) {
        MOBILE(DownloadManager.Request.NETWORK_MOBILE),
        WIFI(DownloadManager.Request.NETWORK_WIFI)
    }
}