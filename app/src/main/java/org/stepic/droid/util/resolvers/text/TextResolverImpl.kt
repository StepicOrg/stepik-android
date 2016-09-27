package org.stepic.droid.util.resolvers.text

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import org.stepic.droid.util.resolvers.CoursePropertyResolver

class TextResolverImpl : TextResolver {
    companion object {
        val tagHandler = OlLiTagHandler()
    }

    @Suppress("DEPRECATION")
    override fun resolveCourseProperty(type: CoursePropertyResolver.Type, content: String?, context: Context): TextResult {
        if (content == null) {
            return TextResult("", false)
        }

        if (type == CoursePropertyResolver.Type.summary ||
                type == CoursePropertyResolver.Type.requirements ||
                type == CoursePropertyResolver.Type.description) {
            //it can be html text

            val fromHtml: Spanned
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fromHtml = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, tagHandler)
            } else {
                fromHtml = Html.fromHtml(content, null, tagHandler)
            }

            return TextResult(fromHtml,
                    isNeedWebView = false) //FIXME handle latex & ImageGetter & <pre><code>
        } else {
            return TextResult(content.trim())
        }
    }
}
