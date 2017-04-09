package org.stepic.droid.util.resolvers.text

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.resolvers.CoursePropertyResolver
import javax.inject.Inject

class TextResolverImpl
@Inject constructor(config: Config) : TextResolver {

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
            return TextResult(content, isNeedWebView = true)
        }

    }

    override fun resolveCourseProperty(type: CoursePropertyResolver.Type, content: String?, context: Context): TextResult {
        if (content == null) {
            return TextResult("")
        }

        if (type == CoursePropertyResolver.Type.summary ||
                type == CoursePropertyResolver.Type.requirements ||
                type == CoursePropertyResolver.Type.description) {
            //it can be html text

            if (HtmlHelper.isForWebView(content)) {
                return TextResult(content,
                        isNeedWebView = true)
            } else {
                val fromHtml: CharSequence = fromHtml(content).trim()
                return TextResult(fromHtml,
                        isNeedWebView = false)
            }

        } else {
            return TextResult(content.trim())
        }
    }

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

    override fun replaceWhitespaceToBr(answer: String?): String {
        if (answer == null) return ""

        val newContent = answer.replace("\n", "<br>")
        return newContent
    }
}
