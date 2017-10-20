package org.stepic.droid.code.highlight.themes

import android.support.annotation.ColorInt
import org.stepic.droid.code.highlight.prettify.parser.Prettify.*

data class CodeSyntax(
        @ColorInt val plain: Int,
        @ColorInt val string: Int = plain,
        @ColorInt val keyword: Int = plain,
        @ColorInt val comment: Int = plain,
        @ColorInt val type: Int = plain,
        @ColorInt val literal: Int = plain,
        @ColorInt val punctuation: Int = plain,
        @ColorInt val tag: Int = plain,
        @ColorInt val declaration: Int = plain,
        @ColorInt val source: Int = plain,
        @ColorInt val attributeName: Int = plain,
        @ColorInt val attributeValue: Int = plain,
        @ColorInt val nocode: Int = plain
) {

    fun shouldBePainted(prType: String) = colorMap[prType] != plain

    val colorMap = hashMapOf(
            PR_STRING       to string,
            PR_KEYWORD      to keyword,
            PR_COMMENT      to comment,
            PR_TYPE         to type,
            PR_LITERAL      to literal,
            PR_PUNCTUATION  to punctuation,
            PR_PLAIN        to plain,
            PR_TAG          to tag,
            PR_DECLARATION  to declaration,
            PR_SOURCE       to source,
            PR_ATTRIB_NAME  to attributeName,
            PR_ATTRIB_VALUE to attributeValue,
            PR_NOCODE       to nocode
    )

}