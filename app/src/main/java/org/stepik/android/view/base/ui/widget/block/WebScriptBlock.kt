package org.stepik.android.view.base.ui.widget.block

class WebScriptBlock : ContentBlock {
    override fun isEnabled(content: String): Boolean =
        "wysiwyg-" in content ||
        "<h" in content ||
        "<img" in content ||
        "<iframe" in content ||
        "<audio" in content
}