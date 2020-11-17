package org.stepik.android.view.in_app_web_view.routing

import android.net.Uri
import javax.inject.Inject

class InAppWebViewUrlProcessor
@Inject
constructor() {
    companion object {
        const val PARAM_EMBEDDED = "embedded"
    }

    fun processInAppWebViewUrl(url: String): String =
        Uri.parse(url)
            .buildUpon()
            .appendQueryParameter(PARAM_EMBEDDED, "true")
            .build()
            .toString()
}