package org.stepik.android.view.latex.model.block

import androidx.annotation.ColorInt
import org.stepik.android.domain.latex.model.block.ContentBlock

class TextColorBlock(
    @ColorInt
    textColor: Int
) : ContentBlock {
    override val preBody: String =
        "<font color='${Integer.toHexString(textColor).substring(2)}'>"

    override val postBody: String =
        "</font>"
}