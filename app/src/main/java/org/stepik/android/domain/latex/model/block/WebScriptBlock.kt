package org.stepik.android.domain.latex.model.block

class WebScriptBlock : ContentBlock {
    override fun isEnabled(content: String): Boolean =
        "wysiwyg-" in content ||
        "<h1" in content ||
        "<h2" in content ||
        "<h3" in content ||
        "<h4" in content ||
        "<h5" in content ||
        "<h6" in content ||
        "<img" in content ||
        "<iframe" in content ||
        "<audio" in content ||
        "<table" in content ||
        "<div" in content
}