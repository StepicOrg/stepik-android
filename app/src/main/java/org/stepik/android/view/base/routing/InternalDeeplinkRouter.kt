package org.stepik.android.view.base.routing

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.stepic.droid.BuildConfig
import org.stepic.droid.R

object InternalDeeplinkRouter {
    private const val PARAM_MOBILE_INTERNAL_DEEPLINK = "mobile_internal_deeplink"

    fun openInternalDeeplink(context: Context, uri: Uri, fallback: () -> Unit = { openInExternal(context, uri) }) {
        try {
            val path = uri.buildUpon()
                .appendQueryParameter(PARAM_MOBILE_INTERNAL_DEEPLINK, "true")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, path)
            intent.`package` = BuildConfig.APPLICATION_ID
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            fallback.invoke()
        }
    }

    /**
     * context.startActivity will automatically infer what application to use or offer a choice
     */
    private fun openInExternal(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val errorMessage = context.resources.getString(R.string.internal_deeplink_error, uri.toString())
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}