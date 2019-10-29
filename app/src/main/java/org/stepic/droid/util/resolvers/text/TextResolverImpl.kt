package org.stepic.droid.util.resolvers.text

import android.os.Build
import android.text.Html
import android.text.Spanned
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
        val tagHandler = OlLiTagHandler()
    }

    override fun resolveStepText(content: String?): TextResult {
        if (content == null) {
            return TextResult("")
        }

        val isForWebView = HtmlHelper.isForWebView(content)

        if (!isForWebView) {
            val fromHtml = HtmlHelper.trimTrailingWhitespace(fromHtml(content))
            return TextResult(fromHtml)
        } else {
            return TextResult(prepareStepTextForWebView(content), isNeedWebView = true)
        }

    }

    // Remove &nbsp; characters from html text in order to fit text in screen properly.
    // Often this char is inserted in text by text editor without grammar reasons.
    private fun prepareStepTextForWebView(content: String): String =
            content.replace('\u00A0', ' ')

    @Suppress("DEPRECATION")
    override fun fromHtml(content: String?): CharSequence {
        if (content == null) return ""
        val fromHtml: Spanned
        val textWithBaseUrl = content.replace("href=\"/", "href=\"$baseUrl/")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fromHtml = Html.fromHtml(textWithBaseUrl, Html.FROM_HTML_MODE_LEGACY, null, tagHandler)
        } else {
            fromHtml = Html.fromHtml(textWithBaseUrl, null, tagHandler)
        }
        return fromHtml
    }
}
