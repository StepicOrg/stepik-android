package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatEditText
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import org.stepic.droid.code.highlight.prettify.PrettifyParser
import org.stepic.droid.code.highlight.syntaxhighlight.ParseResult

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

    fun setTheme(theme: Map<String, Long>) {
        theme["pln"]?.let {
            setTextColor(it.toInt())
        }
        this.theme = theme
    }

    private val themes = arrayOf(
            mapOf( // github theme
                    "str" to 0xFFd14d14,
                    "kwd" to 0xFF333333,
                    "com" to 0xFF998998,
                    "typ" to 0xFF458458,
                    "lit" to 0xFF458458,
                    "pun" to 0xFF333333,
                    "opn" to 0xFF333333,
                    "clo" to 0xFF333333,
                    "tag" to 0xFF000080,
                    "atn" to 0xFF000080,
                    "atv" to 0xFFd14d14,
                    "dec" to 0xFF333333,
                    "var" to 0xFF008080,
                    "fun" to 0xFF900900
            ),
            mapOf( // tmrw
                    "str" to 0xFFb5bd68,
                    "kwd" to 0xFFb294bb,
                    "com" to 0xFF969896,
                    "typ" to 0xFF81a2be,
                    "lit" to 0xFFde935f,
                    "pun" to 0xFFc5c8c6,
                    "opn" to 0xFFc5c8c6,
                    "clo" to 0xFFc5c8c6,
                    "tag" to 0xFFcc6666,
                    "atn" to 0xFFde935f,
                    "atv" to 0xFF8abeb7,
                    "dec" to 0xFFde935f,
                    "var" to 0xFFcc6666,
                    "fun" to 0xFF81a2be
            ),
            mapOf( // tmrw
                    "str" to 0xFF718c00,
                    "kwd" to 0xFF8959a8,
                    "com" to 0xFF8e908c,
                    "typ" to 0xFF4271ae,
                    "lit" to 0xFFf5871f,
                    "pun" to 0xFF4d4d4c,
                    "opn" to 0xFF4d4d4c,
                    "clo" to 0xFF4d4d4c,
                    "tag" to 0xFFc82829,
                    "atn" to 0xFFf5871f,
                    "atv" to 0xFF3e999f,
                    "dec" to 0xFFf5871f,
                    "var" to 0xFFc82829,
                    "fun" to 0xFF4271ae,
                    "pln" to 0xFF4d4d4c
            ),
            mapOf( // tmrw
                    "str" to 0xFFffce54,
                    "kwd" to 0xFF4fc1e9,
                    "com" to 0xFF656d78,
                    "typ" to 0xFF4fc1e9,
                    "lit" to 0xFFac92ec,
                    "pun" to 0xFFe6e9ed,
                    "opn" to 0xFFe6e9ed,
                    "clo" to 0xFFe6e9ed,
                    "tag" to 0xFFed5565,
                    "atn" to 0xFFa0d468,
                    "atv" to 0xFFffce54,
                    "dec" to 0xFFac92ec,
                    "var" to 0xFFe6e9ed,
                    "fun" to 0xFFe6e9ed,
                    "pln" to 0xFFe6e9ed
            ),
            mapOf( // tmrw
                    "str" to 0xFFc18401,
                    "kwd" to 0xFF0184bc,
                    "com" to 0xFF999999,
                    "lit" to 0xFF50a14f,
                    "pun" to 0xFF4078f2,
                    "fun" to 0xFFe45649
            )
    )

    private var theme : Map<String, Long> = themes[4]

    private val lineNumbersBackground by lazy {
        val p = Paint()
        p.color = Color.argb(0xFF, 0xEE, 0xEE, 0xEE)
        p
    }

    private val lineNumbersStroke by lazy {
        val p = Paint()
        p.color = Color.argb(0xFF, 0xCC, 0xCC, 0xCC)
        p
    }

    private val lineNumbersSelectedLine by lazy {
        val p = Paint()
        p.color = Color.argb(0x44, 0xCC, 0xCC, 0xCC)
        p
    }

    private val lineNumbersColor by lazy {
        val p = Paint()
        p.color = Color.argb(0xFF, 0x33, 0x33, 0x33)
        p.typeface = Typeface.MONOSPACE
        p.textAlign = Paint.Align.RIGHT
        p.textSize = textSize * 0.8f
        p.flags = p.flags or Paint.ANTI_ALIAS_FLAG
        p
    }

    private val rect = Rect()

    override fun onDraw(canvas: Canvas) {
        val linesOffset = lineCount.toString().length * 32

        setPadding(linesOffset + 8, paddingTop, paddingRight, paddingBottom)
        canvas.drawRect(0f, 0f, linesOffset.toFloat(), height.toFloat(), lineNumbersBackground)
        canvas.drawLine(linesOffset.toFloat(), 0f, linesOffset.toFloat(), height.toFloat(), lineNumbersStroke)

        val lt = layout
        if (lt != null) {
            val lines = text.split("\n")
            var pos = 0
            val poses = lines.map {
                val line = lt.getLineForOffset(pos)
                pos += it.length + 1
                line
            }
            pos = 1
            poses.forEach {
                val y = getLineBounds(it, rect)
                canvas.drawText(pos.toString(), linesOffset.toFloat() - 8, y.toFloat(), lineNumbersColor)
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

                canvas.drawRect(0f, top, width.toFloat(), bottom, lineNumbersSelectedLine)


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
        Log.d(javaClass.canonicalName, parseResults.toString())
        parseResults.filterNot { it.styleKeysString == "pln" }.forEach { pr ->
            theme[pr.styleKeysString]?.let {
                sp.setSpan(ForegroundColorSpan(it.toInt()), pr.offset, pr.offset + pr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}