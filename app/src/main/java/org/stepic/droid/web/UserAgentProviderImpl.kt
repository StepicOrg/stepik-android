package org.stepic.droid.web

import android.content.Context

class UserAgentProviderImpl(private val context: Context) : UserAgentProvider {

    val userAgent by lazy {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        with(packageInfo) {
            "client = android, versionName = $versionName, versionCode = $versionCode"
        }
    }

    override fun provideUserAgent() = userAgent
}
