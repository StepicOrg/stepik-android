package org.stepic.droid.util

import android.app.Application
import com.gu.toolargetool.TooLargeTool
import okhttp3.Interceptor

object DebugToolsHelper {
    fun initDebugTools(app: Application) {
        TooLargeTool.startLogging(app)
    }

    fun getDebugInterceptors(): List<Interceptor> =
        emptyList()
}
