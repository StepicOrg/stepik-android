package org.stepik.android.domain.latex.model.rule

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class ReplaceModelViewWithImage : ContentRule {
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
        .attr("style", "height: 200px; width: 200px; background-image: url($src);")
        .attr("onClick", "handleARModel('$href')")
}