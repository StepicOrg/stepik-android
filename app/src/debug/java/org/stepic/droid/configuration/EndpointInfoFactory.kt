package org.stepic.droid.configuration

import android.content.Context
import com.google.gson.Gson
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.debug.model.EndpointConfig
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.inject.Inject

class EndpointInfoFactory
@Inject
constructor(
    private val context: Context,
    private val gson: Gson,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val ENDPOINT_DEV_CONFIG = "config_dev.json"
        private const val ENDPOINT_PRODUCTION_CONFIG = "config_production.json"
        private const val ENDPOINT_RELEASE_CONFIG = "config_release.json"
    }
    fun createEndpointInfo(): EndpointInfo {
        val fileName =
            when (EndpointConfig.values()[sharedPreferenceHelper.endpointConfig]) {
                EndpointConfig.DEV ->
                    ENDPOINT_DEV_CONFIG
                EndpointConfig.PRODUCTION ->
                    ENDPOINT_PRODUCTION_CONFIG
                EndpointConfig.RELEASE ->
                    ENDPOINT_RELEASE_CONFIG
            }
        return context.assets.open("configs/${fileName}").use {
            gson.fromJson(InputStreamReader(it, Charset.defaultCharset()), EndpointInfo::class.java)
        }
    }
}