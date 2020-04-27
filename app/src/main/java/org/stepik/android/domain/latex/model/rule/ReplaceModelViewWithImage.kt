package org.stepik.android.domain.latex.model.rule

import org.stepic.droid.util.toPx

class ReplaceModelViewWithImage : ContentRule {
    companion object {
        private const val BORDER_RADIUS_DP = 8f

        private val modelViewerRegex = Regex("<model-viewer\\s*.*>\\s*.*</model-viewer>")
        private val thumbnailRegex = Regex("thumbnail=\"(.*?)\"")
        private val srcRegex = Regex("\\ssrc=\"(.*?)\"")
    }

    override fun process(content: String): String =
        modelViewerRegex.replace(content) { matchResult ->
            val matchResultValue = matchResult.value ?: ""
            val src = thumbnailRegex.find(matchResultValue)?.groups?.get(1)?.value ?: ""
            val href = srcRegex.find(matchResultValue)?.groups?.get(1)?.value ?: ""
            formARElement(src, href)
        }

    private fun formARElement(src: String, href: String): String =
        "<div style=\"background-image: url($src); border-radius: ${BORDER_RADIUS_DP.toPx()}px;\" " +
                "class=\"ar-model\" onclick=\"handleARModel('$href')\"></div>"
}