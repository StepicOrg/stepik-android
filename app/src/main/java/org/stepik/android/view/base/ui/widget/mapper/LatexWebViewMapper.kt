package org.stepik.android.view.base.ui.widget.mapper

import android.content.Context
import org.stepic.droid.configuration.Config
import org.stepik.android.view.base.ui.widget.attributes.TextAttributes
import org.stepik.android.view.base.ui.widget.block.BaseStyleBlock
import org.stepik.android.view.base.ui.widget.block.ContentBlock
import org.stepik.android.view.base.ui.widget.block.MetaBlock
import org.stepik.android.view.base.ui.widget.block.SelectionColorStyleBlock
import org.stepik.android.view.base.ui.widget.model.LatexData

class LatexWebViewMapper(
    private val config: Config,
    private val context: Context
) {
    companion object {
        private const val ASSETS = "file:///android_asset/"
        private const val FONTS = "fonts/"
        private const val EXTENSION = ".ttf"
    }

    fun mapLatexData(webData: LatexData.Web, attributes: TextAttributes, width: Int): String {
        val fontPath = "$ASSETS$FONTS${context.resources.getResourceEntryName(attributes.fontResId)}$EXTENSION"

        val blocks =
            listOf(
                BaseStyleBlock(fontPath),
                MetaBlock(config.baseUrl, width),
                SelectionColorStyleBlock(attributes.textColorHighlight)
            )
            .filter { it.isEnabled(webData.body) }

        val header = webData.header + blocks.joinToString(separator = "", transform = ContentBlock::header)
        val preBody = webData.preBody + blocks.joinToString(separator = "", transform = ContentBlock::preBody)
        val postBody = webData.postBody + blocks.joinToString(separator = "", transform = ContentBlock::postBody)

        return """
            <!DOCTYPE html>
            <html>
            <head>
                $header
            </head>
            <body style='margin:0;padding:0;'>
                $preBody
                ${webData.body}
                $postBody
            </body>
            </html>
        """.trimIndent()
    }
}