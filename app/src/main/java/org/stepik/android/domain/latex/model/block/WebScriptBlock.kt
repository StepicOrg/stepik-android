package org.stepik.android.domain.latex.model.block

class WebScriptBlock : ContentBlock {
    override fun isEnabled(content: String): Boolean =
        "wysiwyg-" in content ||
        "<h" in content ||
        "<img" in content ||
        "<iframe" in content ||
        "<audio" in content
}