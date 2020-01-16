package org.stepik.android.view.base.ui.widget.rule

interface ContentRule {
    /**
     * Processes given [content] and return new one
     */
    fun process(content: String): String
}