package org.stepik.android.view.latex.model.block

import androidx.annotation.ColorInt
import org.stepik.android.domain.latex.model.block.ContentBlock

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