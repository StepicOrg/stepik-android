package org.stepic.droid.ui.util

import android.annotation.TargetApi
import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.IOException
import java.io.InputStream

/**
 * This class fix problems with loading JS files in assets folder on API 15
 */
class AssetSupportWebViewClient : WebViewClient() {
    companion object {
        private const val assetUrl = "file:///android_asset/"
    }

    @TargetApi(15)
    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val stream = createInputStreamForAsset(view.context, url)
        return if (stream != null) {
            WebResourceResponse("text/javascript", "utf-8", stream)
        } else super.shouldInterceptRequest(view, url)
    }

    private fun createInputStreamForAsset(context: Context, url: String): InputStream? {
        if (url.startsWith(assetUrl)) {
            try {
                val path = Uri.parse(url.substring(assetUrl.length)).path
                return context.assets.open(path, AssetManager.ACCESS_STREAMING)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}