package org.stepik.android.cache.network.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.support.annotation.RequiresApi
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.DownloadConfiguration
import java.lang.Exception
import java.util.EnumSet
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@AppSingleton
class NetworkStateTracker
@Inject
constructor(
    private val context: Context,
    private val connectivityManager: ConnectivityManager
) {
    private val listeners = linkedSetOf<(EnumSet<DownloadConfiguration.NetworkType>) -> Unit>()

    private val reentrantLock = ReentrantLock()

    private var state: EnumSet<DownloadConfiguration.NetworkType> = EnumSet.noneOf(DownloadConfiguration.NetworkType::class.java)
        set(value) {
            reentrantLock.withLock {
                if (field != value) {
                    field = value
                    listeners.forEach { it(value) }
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    private lateinit var networkStateCallback: NetworkStateCallback
    private lateinit var networkStateBroadcastReceiver: NetworkStateBroadcastReceiver

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkStateCallback = NetworkStateCallback()
        } else {
            networkStateBroadcastReceiver = NetworkStateBroadcastReceiver()
        }
    }

    fun getNetworkState(): EnumSet<DownloadConfiguration.NetworkType> {
        reentrantLock.withLock {
            state = getNetworkTypes()
            return state
        }
    }

    private fun getNetworkTypes(): EnumSet<DownloadConfiguration.NetworkType> {
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

    fun addListener(listener: (EnumSet<DownloadConfiguration.NetworkType>) -> Unit) {
        reentrantLock.withLock {
            if (listeners.add(listener)) {
                if (listeners.size == 1) {
                    state = getNetworkTypes()
                    startTracking()
                }
            }
        }
    }

    fun removeListener(listener: (EnumSet<DownloadConfiguration.NetworkType>) -> Unit) {
        reentrantLock.withLock {
            if (listeners.remove(listener) && listeners.isEmpty()) {
                stopTracking()
            }
        }
    }

    private fun startTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkStateCallback)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(networkStateBroadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    private fun stopTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                connectivityManager.unregisterNetworkCallback(networkStateCallback)
            } catch (_: Exception) {}
        } else {
            context.unregisterReceiver(networkStateBroadcastReceiver)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    inner class NetworkStateCallback : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
            state = getNetworkTypes()
        }

        override fun onLost(network: Network?) {
            state = getNetworkTypes()
        }
    }

    inner class NetworkStateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            @Suppress("DEPRECATION")
            if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                state = getNetworkTypes()
            }
        }
    }
}