package org.stepik.android.domain.base

import android.net.Uri
import android.text.style.URLSpan
import android.view.View
import org.stepik.android.view.base.routing.InternalDeeplinkRouter

open class InternalDeeplinkURLSpan(url: String) : URLSpan(url) {
    override fun onClick(widget: View) {
        InternalDeeplinkRouter
            .openInternalDeeplink(widget.context, Uri.parse(url))
    }
}