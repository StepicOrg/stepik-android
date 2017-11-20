package org.stepic.droid.util

import android.text.SpannableString
import android.text.SpannableStringBuilder

private const val STRING_FORMAT = "%s"

fun SpannableString.format(vararg spannableStrings: SpannableString): SpannableString {
    val builder = SpannableStringBuilder(this)

    var pos = 0
    for (spannable in spannableStrings) {
        pos = builder.indexOf(STRING_FORMAT, pos)
        if (pos == -1) break

        builder.replace(pos, pos + STRING_FORMAT.length, spannable)
        pos += spannable.length
    }

    return SpannableString(builder)
}