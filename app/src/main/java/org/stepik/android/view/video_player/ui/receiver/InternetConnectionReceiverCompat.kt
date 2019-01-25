package org.stepik.android.view.video_player.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.Looper

class InternetConnectionReceiverCompat(
    private val onInternetConnectionAvailable: () -> Unit
) : BroadcastReceiver() {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                private val mainHandler = Handler(Looper.getMainLooper())

                override fun onAvailable(network: Network?) {
                    mainHandler.post {
                        onInternetConnectionAvailable()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == android.net.ConnectivityManager.CONNECTIVITY_ACTION && isConnectionAvailable(context)) {
            onInternetConnectionAvailable()
        }
    }

    @Suppress("DEPRECATION")
    private fun isConnectionAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    fun registerReceiver(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            @Suppress("DEPRECATION")
            context.registerReceiver(this, IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION))
        } else {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let(connectivityManager::registerDefaultNetworkCallback)
        }
    }

    fun unregisterReceiver(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            context.unregisterReceiver(this)
        } else {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let(connectivityManager::unregisterNetworkCallback)
        }
    }
}