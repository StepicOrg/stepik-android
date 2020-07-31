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
        Stetho.initializeWithDefaults(context)

        SoLoader.init(context, false)

        val client = AndroidFlipperClient.getInstance(context)
        client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
        client.addPlugin(DatabasesFlipperPlugin(context))
        client.addPlugin(SharedPreferencesFlipperPlugin(context))
        client.addPlugin(NavigationFlipperPlugin.getInstance())
        client.addPlugin(networkPlugin)

        client.start()
    }

    fun getDebugInterceptors(): List<Interceptor> =
        emptyList()
}
