package org.stepik.android.domain.latex.model

sealed class LatexData {
    data class Text(val text: CharSequence): LatexData()
    data class Web(val header: String, val preBody: String, val body: String, val postBody: String): LatexData()
}