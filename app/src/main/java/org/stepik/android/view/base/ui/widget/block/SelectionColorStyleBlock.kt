package org.stepik.android.view.base.ui.widget.block

import androidx.annotation.ColorInt

class SelectionColorStyleBlock(
    @ColorInt
    selectionColor: Int
) : ContentBlock {
    override val header: String = """
        <style>
            ::selection { background: #${Integer.toHexString(selectionColor).substring(2)}; }
        </style>
    """.trimIndent()

    override fun isEnabled(content: String): Boolean = true
}