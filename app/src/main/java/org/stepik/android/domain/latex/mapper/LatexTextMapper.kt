package org.stepik.android.domain.latex.mapper

import android.text.style.URLSpan
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.resolvers.text.OlLiTagHandler
import org.stepik.android.domain.base.InternalDeeplinkURLSpan
import org.stepik.android.domain.latex.model.block.ContentBlock
import org.stepik.android.domain.latex.model.block.HighlightScriptBlock
import org.stepik.android.domain.latex.model.block.HorizontalScrollBlock
import org.stepik.android.domain.latex.model.block.KotlinRunnableSamplesScriptBlock
import org.stepik.android.domain.latex.model.block.LatexScriptBlock
import org.stepik.android.domain.latex.model.block.MinVisibleBlock
import org.stepik.android.domain.latex.model.block.WebScriptBlock
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.domain.latex.model.block.MetaBlock
import org.stepik.android.domain.latex.model.block.ModelViewerBlock
import org.stepik.android.domain.latex.model.rule.RelativePathContentRule
import org.stepik.android.domain.latex.model.rule.ReplaceModelViewWithImage
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

    private val regularBlocks =
        listOf(
            HorizontalScrollBlock(),
            MinVisibleBlock(),
            MetaBlock(config.baseUrl),
            ModelViewerBlock()
        )

    private val rules =
        listOf(
            RelativePathContentRule(config.baseUrl),
            ReplaceModelViewWithImage()
        )

    private val tagHandler = OlLiTagHandler()

    fun mapToLatexText(text: String): LatexData {
        val content = rules.fold(text) { tmp, rule -> rule.process(tmp) }
        val primary = primaryBlocks.filter { it.isEnabled(content) }

        return if (primary.isEmpty()) {
            val spanned = HtmlCompat
                .fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler)
                .trimEnd(Char::isWhitespace)
                .toSpannable()

            for (span in spanned.getSpans<URLSpan>()) {
                val start = spanned.getSpanStart(span)
                val end = spanned.getSpanEnd(span)
                val flags = spanned.getSpanFlags(span)

                spanned.removeSpan(span)
                spanned.setSpan(InternalDeeplinkURLSpan(span.url), start, end, flags)
            }

            LatexData.Text(spanned)
        } else {
            val blocks = primary + regularBlocks.filter { it.isEnabled(content) }

            LatexData.Web(
                header = blocks.joinToString(separator = "", transform = ContentBlock::header),
                preBody = blocks.joinToString(separator = "", transform = ContentBlock::preBody),
                body = content,
                postBody = blocks.asReversed().joinToString(separator = "", transform = ContentBlock::postBody)
            )
        }
    }
}