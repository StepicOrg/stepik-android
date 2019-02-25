package org.stepik.android.data.network.repository

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import io.reactivex.Single
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.domain.network.repository.NetworkTypeRepository
import java.util.EnumSet
import javax.inject.Inject

class NetworkTypeRepositoryImpl
@Inject
constructor(
    private val connectivityManager: ConnectivityManager,
    private val userPreferences: UserPreferences
) : NetworkTypeRepository {

    override fun getAvailableNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        Single.fromCallable(::getNetworkTypesCompat)

    private fun getNetworkTypesCompat(): EnumSet<DownloadConfiguration.NetworkType> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val networkInfo = connectivityManager
                .activeNetworkInfo
                ?.takeIf(NetworkInfo::isConnected)

            @Suppress("DEPRECATION")
            return when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI ->
                    EnumSet.of(DownloadConfiguration.NetworkType.WIFI)

                ConnectivityManager.TYPE_MOBILE ->
                    EnumSet.of(DownloadConfiguration.NetworkType.MOBILE)

                else ->
                    EnumSet.noneOf(DownloadConfiguration.NetworkType::class.java)
            }
        } else {
            val networkCapabilities = connectivityManager
                .activeNetwork
                ?.let(connectivityManager::getNetworkCapabilities)
                ?: return EnumSet.noneOf(DownloadConfiguration.NetworkType::class.java)

            return EnumSet.copyOf(listOfNotNull(
                DownloadConfiguration.NetworkType.WIFI
                    ?.takeIf {
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    },

                DownloadConfiguration.NetworkType.MOBILE
                    ?.takeIf { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) }
            ))
        }
    }

    override fun getAllowedNetworkTypes(): Single<EnumSet<DownloadConfiguration.NetworkType>> =
        Single.fromCallable {
            if (userPreferences.isNetworkMobileAllowed) {
                EnumSet.of(DownloadConfiguration.NetworkType.MOBILE, DownloadConfiguration.NetworkType.WIFI)
            } else {
                EnumSet.of(DownloadConfiguration.NetworkType.WIFI)
            }
        }
}