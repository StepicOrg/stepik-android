package org.stepic.droid.util

import android.app.Application
import okhttp3.Interceptor

object DebugToolsHelper {
    fun initDebugTools(app: Application) {
        // no op
    }

    /***
     *  This function can be used to debug non-main processes
     */
    fun initDebugTools(context: Context) {
        // no op
    }

    fun getDebugInterceptors(): List<Interceptor> =
        emptyList()
}
