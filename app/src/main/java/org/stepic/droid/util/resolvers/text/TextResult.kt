package org.stepic.droid.util.resolvers.text

data class TextResult(
    val text: CharSequence,
    val isNeedWebView: Boolean = false,
    val isNeedLaTeX: Boolean = false
)