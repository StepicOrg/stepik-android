package org.stepik.android.view.latex.mapper

import android.content.Context
import org.stepic.droid.configuration.Config
import org.stepik.android.view.latex.model.TextAttributes
import org.stepik.android.view.latex.model.block.BaseStyleBlock
import org.stepik.android.domain.latex.model.block.ContentBlock
import org.stepik.android.view.latex.model.block.MetaBlock
import org.stepik.android.view.latex.model.block.SelectionColorStyleBlock
import org.stepik.android.view.latex.model.block.TextColorBlock
import org.stepik.android.domain.latex.model.LatexData
import javax.inject.Inject

class LatexWebViewMapper
@Inject
constructor(
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
                MetaBlock(
                    config.baseUrl,
                    width
                ),
                SelectionColorStyleBlock(
                    attributes.textColorHighlight
                ),
                TextColorBlock(
                    attributes.textColor
                )
            )

        val header = webData.header + blocks.joinToString(separator = "", transform = ContentBlock::header)
        val preBody = webData.preBody + blocks.joinToString(separator = "", transform = ContentBlock::preBody)
        val postBody = blocks.asReversed().joinToString(separator = "", transform = ContentBlock::postBody) + webData.postBody // order is reversed to preserve tags

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