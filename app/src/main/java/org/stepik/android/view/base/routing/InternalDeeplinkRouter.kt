package org.stepik.android.view.base.routing

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.stepic.droid.BuildConfig

object InternalDeeplinkRouter {
    private const val PARAM_MOBILE_INTERNAL_DEEPLINK = "mobile_internal_deeplink"

    fun openInternalDeeplink(context: Context, uri: Uri) {
        try {
            val path = uri.buildUpon()
                .appendQueryParameter(PARAM_MOBILE_INTERNAL_DEEPLINK, "true")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, path)
            intent.`package` = BuildConfig.APPLICATION_ID
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}