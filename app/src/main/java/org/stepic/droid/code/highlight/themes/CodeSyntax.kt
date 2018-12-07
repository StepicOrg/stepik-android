package org.stepic.droid.code.highlight.themes

import android.support.annotation.ColorInt
import org.stepic.droid.code.highlight.prettify.parser.Prettify.*

class CodeSyntax(
        @ColorInt val plain: Int,
        @ColorInt string: Int = plain,
        @ColorInt keyword: Int = plain,
        @ColorInt comment: Int = plain,
        @ColorInt type: Int = plain,
        @ColorInt literal: Int = plain,
        @ColorInt punctuation: Int = plain,
        @ColorInt tag: Int = plain,
        @ColorInt declaration: Int = plain,
        @ColorInt source: Int = plain,
        @ColorInt attributeName: Int = plain,
        @ColorInt attributeValue: Int = plain,
        @ColorInt nocode: Int = plain
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