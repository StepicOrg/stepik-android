package org.stepik.android.domain.latex.model.block

interface ContentBlock {
    val header: String
        get() = ""

    val preBody: String
        get() = ""

    val postBody: String
        get() = ""

    fun isEnabled(content: String): Boolean =
        true
}