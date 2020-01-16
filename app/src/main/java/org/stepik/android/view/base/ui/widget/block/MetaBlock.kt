package org.stepik.android.view.base.ui.widget.block

class MetaBlock(
    baseUrl: String,
    width: Int
) : ContentBlock {
    override val header: String = """
        "<meta name="viewport" content="width=$width, user-scalable=no, target-densitydpi=medium-dpi" />
        "<base href="$baseUrl">
    """.trimIndent()

    override fun isEnabled(content: String): Boolean = true
}