package org.stepik.android.domain.latex.model.rule

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.stepic.droid.util.toPx

class ReplaceModelViewWithImage : ContentRule {
    companion object {
        private const val BORDER_RADIUS_DP = 8f
    }

    override fun process(content: String): String {
        val document = Jsoup.parse(content)

        val modelViewerTags = document.select("model-viewer")

        modelViewerTags.forEach { modelViewerTag ->
            val src = modelViewerTag.attr("thumbnail")
            val href = modelViewerTag.attr("src")
            modelViewerTag.replaceWith(formARElement(src, href))
        }
        return document.toString()
    }

    private fun formARElement(src: String, href: String): Element =
        Element("div")
        .attr("style", "background-image: url($src); border-radius: ${BORDER_RADIUS_DP.toPx()}px;")
        .attr("class", "ar-model")
        .attr("onClick", "handleARModel('$href')")
}