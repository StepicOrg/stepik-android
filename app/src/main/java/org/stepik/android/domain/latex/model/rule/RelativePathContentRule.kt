package org.stepik.android.domain.latex.model.rule

class RelativePathContentRule(
    private val baseUrl: String
) : ContentRule {
    override fun process(content: String): String =
        content.replace("href=\"/", "href=\"$baseUrl/")
}