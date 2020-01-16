package org.stepik.android.view.base.ui.widget.block

interface ContentBlock {
    val header: String
        get() = ""

    val preBody: String
        get() = ""

    val postBody: String
        get() = ""

    fun isEnabled(content: String): Boolean
}