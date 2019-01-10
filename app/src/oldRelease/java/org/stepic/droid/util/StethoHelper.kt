package org.stepic.droid.util

import android.app.Application
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.Interceptor

object StethoHelper {
    fun initStetho(app: Application) {
        Stetho.initializeWithDefaults(app)
    }

    fun getInterceptor(): Interceptor =
        StethoInterceptor()
}
