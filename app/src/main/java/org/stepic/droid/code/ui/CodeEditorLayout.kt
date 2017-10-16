package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import org.stepic.droid.R
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.Presets
import org.stepic.droid.util.insertText

class CodeEditorLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ScrollView(context, attrs, defStyleAttr) {
    private val codeEditor: CodeEditor

    var theme: CodeTheme
        get() = codeEditor.theme
        set(value) {
            setBackgroundColor(value.background)
            codeEditor.theme = value
        }

    val text: CharSequence
        get() = codeEditor.text.toString()

    var lang: String
        get() = codeEditor.lang
        set(value) {
            codeEditor.lang = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_code_editor, this, true)
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

    fun setText(text: String?) {
        text?.let { codeEditor.setText(it) }
    }

    fun insertText(text: String) = codeEditor.insertText(text)

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        codeEditor.isEnabled = enabled
    }
}