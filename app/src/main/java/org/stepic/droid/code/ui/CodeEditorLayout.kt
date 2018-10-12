package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.Typeface
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.view.LayoutInflater
import org.stepic.droid.R
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.Presets
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.util.insertText

class CodeEditorLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : NestedScrollView(context, attrs, defStyleAttr) {
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

    val indentSize: Int
        get() = codeEditor.indentSize

    var codeToolbarAdapter: CodeToolbarAdapter?
        get() = codeEditor.codeToolbarAdapter
        set(value) {
            codeEditor.codeToolbarAdapter = value
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

    fun insertText(text: String, offset : Int) = codeEditor.insertText(text, offset)

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        codeEditor.isEnabled = enabled
    }
}