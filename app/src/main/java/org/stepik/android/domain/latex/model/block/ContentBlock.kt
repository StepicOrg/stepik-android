package org.stepik.android.domain.latex.model.block

import org.stepik.android.domain.latex.model.Settings

interface ContentBlock {
    val header: String
        get() = ""

    val preBody: String
        get() = ""

    val postBody: String
        get() = ""

    val settings: Settings
        get() = Settings.DEFAULT_SETTINGS

    fun isEnabled(content: String): Boolean =
        true
}