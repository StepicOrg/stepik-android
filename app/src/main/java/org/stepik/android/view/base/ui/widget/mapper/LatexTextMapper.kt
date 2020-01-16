package org.stepik.android.view.base.ui.widget.mapper

import androidx.core.text.HtmlCompat
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.resolvers.text.OlLiTagHandler
import org.stepik.android.view.base.ui.widget.block.ContentBlock
import org.stepik.android.view.base.ui.widget.block.HighlightScriptBlock
import org.stepik.android.view.base.ui.widget.block.HorizontalScrollBlock
import org.stepik.android.view.base.ui.widget.block.KotlinRunnableSamplesScriptBlock
import org.stepik.android.view.base.ui.widget.block.LatexScriptBlock
import org.stepik.android.view.base.ui.widget.block.MinVisibleBlock
import org.stepik.android.view.base.ui.widget.block.WebScriptBlock
import org.stepik.android.view.base.ui.widget.model.LatexData
import org.stepik.android.view.base.ui.widget.rule.RelativePathContentRule

class LatexTextMapper(
    config: Config
) {
    private val primaryBlocks =
        listOf(
            HighlightScriptBlock(),
            KotlinRunnableSamplesScriptBlock(),
            LatexScriptBlock(),
            WebScriptBlock()
        )

    private val blocks =
        listOf(
            HorizontalScrollBlock(),
            MinVisibleBlock()
        )

    private val rules =
        listOf(
            RelativePathContentRule(config.baseUrl)
        )

    private val tagHandler = OlLiTagHandler()

    fun mapToLatexText(text: String): LatexData {
        val content = rules.fold(text) { tmp, rule -> rule.process(tmp) }
        val primary = primaryBlocks.filter { it.isEnabled(content) }

        return if (primary.isEmpty()) {
            LatexData.Text(HtmlCompat.fromHtml(content.trimEnd(Char::isWhitespace), HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler))
        } else {
            val blocks = primary + blocks.filter { it.isEnabled(content) }

            LatexData.Web(
                header = blocks.joinToString(separator = "", transform = ContentBlock::header),
                preBody = blocks.joinToString(separator = "", transform = ContentBlock::preBody),
                body = content,
                postBody = blocks.joinToString(separator = "", transform = ContentBlock::postBody)
            )
        }
    }
}