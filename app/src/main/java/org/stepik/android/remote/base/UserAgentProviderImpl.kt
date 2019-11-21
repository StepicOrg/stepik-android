package org.stepik.android.remote.base

import android.content.Context
import javax.inject.Inject

class UserAgentProviderImpl
@Inject constructor(private val context: Context) :
    UserAgentProvider {

    val userAgent by lazy {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val apiLevel = android.os.Build.VERSION.SDK_INT
        with(packageInfo) {
            "StepikDroid/$versionName (Android $apiLevel) build/$versionCode package/$packageName"
        }
    }

    override fun provideUserAgent(): String = userAgent
}
