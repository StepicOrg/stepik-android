package org.stepic.droid.util

import android.app.Application
import okhttp3.Interceptor

object DebugToolsHelper {
    fun initDebugTools(app: Application) {
        // no op
    }

    fun getDebugInterceptors(): List<Interceptor> =
        emptyList()
}
