package org.stepic.droid.util

import android.app.Application
import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.gu.toolargetool.TooLargeTool
import okhttp3.Interceptor

object DebugToolsHelper {
    private val networkPlugin = NetworkFlipperPlugin()

    fun initDebugTools(app: Application) {
        Stetho.initializeWithDefaults(app)
        TooLargeTool.startLogging(app)

        if (FlipperUtils.shouldEnableFlipper(app)) {
            SoLoader.init(app, false)

            val client = AndroidFlipperClient.getInstance(app)
            client.addPlugin(InspectorFlipperPlugin(app, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(app))
            client.addPlugin(SharedPreferencesFlipperPlugin(app))
            client.addPlugin(NavigationFlipperPlugin.getInstance())
            client.addPlugin(networkPlugin)

            client.start()
        }
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
        listOf(
            FlipperOkhttpInterceptor(networkPlugin),
            StethoInterceptor()
        )
}
