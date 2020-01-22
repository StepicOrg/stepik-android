package org.stepik.android.domain.latex.model.rule

interface ContentRule {
    /**
     * Processes given [content] and return new one
     */
    fun process(content: String): String
}