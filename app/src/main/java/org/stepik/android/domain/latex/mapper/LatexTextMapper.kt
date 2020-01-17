package org.stepik.android.domain.latex.mapper

import androidx.core.text.HtmlCompat
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.resolvers.text.OlLiTagHandler
import org.stepik.android.domain.latex.model.block.ContentBlock
import org.stepik.android.domain.latex.model.block.HighlightScriptBlock
import org.stepik.android.domain.latex.model.block.HorizontalScrollBlock
import org.stepik.android.domain.latex.model.block.KotlinRunnableSamplesScriptBlock
import org.stepik.android.domain.latex.model.block.LatexScriptBlock
import org.stepik.android.domain.latex.model.block.MinVisibleBlock
import org.stepik.android.domain.latex.model.block.WebScriptBlock
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.domain.latex.model.rule.RelativePathContentRule
import javax.inject.Inject

class LatexTextMapper
@Inject
constructor(
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
                postBody = blocks.asReversed().joinToString(separator = "", transform = ContentBlock::postBody)
            )
        }
    }
}