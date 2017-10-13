package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import org.stepic.droid.R
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.Presets

class CodeEditorLayout : ScrollView {
    private val codeEditor: CodeEditor

    var theme : CodeTheme
        get() = codeEditor.theme
        set(value) {
            setBackgroundColor(value.background)
            codeEditor.theme = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.code_editor, this, true)
        codeEditor = findViewById(R.id.codeEdit)
        codeEditor.typeface = Typeface.MONOSPACE
        theme = Presets.themes[0]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        codeEditor.scrollContainer = this
    }

    override fun onDetachedFromWindow() {
        codeEditor.scrollContainer = null
        super.onDetachedFromWindow()
    }

    fun setCode(code: CharSequence, lang: String) {
        codeEditor.lang = lang
        codeEditor.setText(code)
    }
}