package org.stepik.android.view.base.routing

import android.net.Uri
import javax.inject.Inject

class ExternalDeepLinkProcessor
@Inject
constructor() {
    companion object {
        private const val PARAM_FROM_MOBILE_APP = "from_mobile_app"
    }

    fun processExternalDeepLing(uriBuilder: Uri.Builder): Uri.Builder =
        uriBuilder
            .appendQueryParameter(PARAM_FROM_MOBILE_APP, "true")
}