package org.stepic.droid.util.resolvers.text

import androidx.core.text.HtmlCompat
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.HtmlHelper
import javax.inject.Inject

class TextResolverImpl
@Inject
constructor(
    config: Config
) : TextResolver {
    private val baseUrl: String = config.baseUrl

    companion object {
        private val tagHandler = OlLiTagHandler()
    }

    // Remove &nbsp; characters from html text in order to fit text in screen properly.
    // Often this char is inserted in text by text editor without grammar reasons.
    private fun prepareStepTextForWebView(content: String): String =
        content.replace('\u00A0', ' ')

    @Suppress("DEPRECATION")
    override fun fromHtml(content: String?): CharSequence =
        content
            ?.replace("href=\"/", "href=\"$baseUrl/")
            ?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY, null, tagHandler) }
            ?: ""
}
