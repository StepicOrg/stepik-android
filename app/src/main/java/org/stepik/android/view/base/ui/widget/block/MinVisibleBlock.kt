package org.stepik.android.view.base.ui.widget.block

import android.os.Build

class MinVisibleBlock : ContentBlock {
    override val postBody: String =
        "<div style=\"height: 1px; overflow: hidden; width: 1px; background-color: rgba(0,0,0,0.001); pointer-events: none; user-select: none; -webkit-user-select: none;\"></div>"

    override fun isEnabled(content: String): Boolean =
        Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
}