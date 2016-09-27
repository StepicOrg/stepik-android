package org.stepic.droid.util.resolvers.text

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.resolvers.CoursePropertyResolver

class TextResolverImpl : TextResolver {
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

            val fromHtml: CharSequence = fromHtml(content).trim()

            return TextResult(fromHtml,
                    isNeedWebView = false) //FIXME handle latex & ImageGetter & <pre><code>
        } else {
            return TextResult(content.trim())
        }
    }

    @Suppress("DEPRECATION")
    private fun fromHtml(content: String): CharSequence {
        val fromHtml: Spanned
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fromHtml = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, tagHandler)
        } else {
            fromHtml = Html.fromHtml(content, null, tagHandler)
        }
        return fromHtml
    }
}
