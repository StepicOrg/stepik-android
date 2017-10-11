package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatEditText
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import org.stepic.droid.code.highlight.prettify.PrettifyParser
import org.stepic.droid.code.highlight.prettify.parser.Prettify.PR_PLAIN
import org.stepic.droid.code.highlight.syntaxhighlight.ParseResult
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.DefaultTheme

class CodeEditor : AppCompatEditText, TextWatcher {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val parser = PrettifyParser()

    var lang = "py"

    init {
        typeface = Typeface.MONOSPACE
        addTextChangedListener(this)
    }


    }

    private val lineNumbersBackgroundPaint = Paint()
    private val lineNumbersStrokePaint = Paint()
    private val selectedLinePaint = Paint()
    private val lineNumbersTextPaint by lazy {
        val p = Paint()
        p.typeface = Typeface.MONOSPACE
        p.textAlign = Paint.Align.RIGHT
        p.textSize = textSize * 0.8f
        p.flags = p.flags or Paint.ANTI_ALIAS_FLAG
        p
    }

    var theme : CodeTheme = DefaultTheme
        set(value) {
            field = value
            setBackgroundColor(value.background)
            setTextColor(value.syntax.plain)
            lineNumbersBackgroundPaint.color = value.lineNumberBackground
            lineNumbersStrokePaint.color = value.lineNumberStroke
            lineNumbersTextPaint.color = value.lineNumberText
            selectedLinePaint.color = value.selectedLineBackground
        }

    private val rect = Rect()

    override fun onDraw(canvas: Canvas) {
        val linesOffset = lineCount.toString().length * lineNumbersTextPaint.textSize.toInt()

        setPadding(linesOffset + 8, paddingTop, paddingRight, paddingBottom)
        canvas.drawRect(0f, 0f, linesOffset.toFloat(), height.toFloat(), lineNumbersBackgroundPaint)
        canvas.drawLine(linesOffset.toFloat(), 0f, linesOffset.toFloat(), height.toFloat(), lineNumbersStrokePaint)

        val lt = layout
        if (lt != null) {
            val lines = text.toString().split("\n")
            var pos = 0
            val poses = lines.map {
                val line = lt.getLineForOffset(pos)
                pos += it.length + 1
                line
            }
            pos = 1
            poses.forEach {
                val y = getLineBounds(it, rect)
                canvas.drawText(pos.toString(), linesOffset.toFloat() - 8, y.toFloat(), lineNumbersTextPaint)
                pos++
            }


            val cursor = selectionStart
            if (cursor > 0) {
                val selectedLine = lt.getLineForOffset(cursor)
                var start = selectedLine
                var end = selectedLine + 1

                while (!poses.contains(start) && start > 0) start--
                while (!poses.contains(end) && end < lineCount) end++

                end -= 1

                getLineBounds(start, rect)
                val top = rect.top.toFloat()
                getLineBounds(end, rect)
                val bottom = rect.bottom.toFloat()

                canvas.drawRect(0f, top, width.toFloat(), bottom, selectedLinePaint)


            }
        }



        super.onDraw(canvas)
    }

    override fun afterTextChanged(p0: Editable?) = updateHighlight()

    override fun beforeTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {}

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {}

    fun updateHighlight() {
        removeSpans(editableText)
        setSpans(editableText, parser.parse(lang, text.toString()))
    }

    private fun removeSpans(editable: Editable) {
        editable.getSpans(0, editable.length, ParcelableSpan::class.java).forEach {
            editable.removeSpan(it)
        }
    }


    private fun setSpans(sp: Spannable, parseResults: List<ParseResult>) {
        parseResults
                .filterNot { it.styleKeysString == PR_PLAIN }
                .forEach { pr ->
            theme.syntax.colorMap[pr.styleKeysString]?.let {
                sp.setSpan(ForegroundColorSpan(it), pr.offset, pr.offset + pr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}