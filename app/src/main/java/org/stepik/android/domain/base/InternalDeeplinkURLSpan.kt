package org.stepik.android.domain.base

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.view.View
import org.stepic.droid.BuildConfig

class InternalDeeplinkURLSpan(url: String) : URLSpan(url) {
    override fun onClick(widget: View) {
        val uri = Uri.parse(url)
        val context = widget.context
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.`package` = BuildConfig.APPLICATION_ID

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            intent.`package` = null
            context.startActivity(intent)
        }
    }
}