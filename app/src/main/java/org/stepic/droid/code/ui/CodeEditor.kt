package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.support.v7.widget.AppCompatEditText
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.ViewTreeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.code.highlight.prettify.PrettifyParser
import org.stepic.droid.code.highlight.syntaxhighlight.ParseResult
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.Presets
import org.stepic.droid.util.DpPixelsHelper
import org.stepic.droid.util.RxEmpty
import java.util.concurrent.TimeUnit

class CodeEditor
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : AppCompatEditText(context, attrs, defStyleAttr), TextWatcher {
    companion object {
        const val SCROLL_DEBOUNCE_MS = 100L
        const val INPUT_DEBOUNCE_MS = 200L
        const val LINE_NUMBERS_MARGIN_DP = 4f
    }

    private val parser = PrettifyParser()

    private val LINE_NUMBERS_MARGIN_PX = DpPixelsHelper.convertDpToPixel(LINE_NUMBERS_MARGIN_DP).toInt()

    private val highlightPublisher = PublishSubject.create<Editable>()
    private val spanPublisher = BehaviorSubject.create<List<ParseResult>>()
    private val layoutChangesPublisher = PublishSubject.create<Any>()

    private val onGlobalLayoutListener  = ViewTreeObserver.OnGlobalLayoutListener  { layoutChangesPublisher.onNext(RxEmpty.INSTANCE) }
    private val onScrollChangedListener = ViewTreeObserver.OnScrollChangedListener { layoutChangesPublisher.onNext(RxEmpty.INSTANCE) }

    private val compositeDisposable = CompositeDisposable()

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

    var lang = ""
        set(value) {
            field = value
            afterTextChanged(editableText) // refresh highlight
        }

    
    internal var scrollContainer: CodeEditorLayout? = null
        set(value) {
            field?.let { container ->
                container.viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    container.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
                } else {
                    container.viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener)
                }
            }

            value?.let { container ->
                container.viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener)
                container.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
            }
            field = value
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        compositeDisposable.add(
                spanPublisher
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { updateHighlight(it) }
        )

        compositeDisposable.add(
                highlightPublisher
                        .debounce(INPUT_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                        .map {
                            parser.parse(lang, it.toString())
                        }
                        .subscribe(spanPublisher::onNext)
        )

        compositeDisposable.add(
                layoutChangesPublisher
                        .debounce(SCROLL_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { spanPublisher.value?.let(this::updateHighlight) }
        )

        addTextChangedListener(this)
        afterTextChanged(editableText)
    }

    override fun onDetachedFromWindow() {
        removeTextChangedListener(this)
        compositeDisposable.dispose()
        super.onDetachedFromWindow()
    }

    var theme : CodeTheme = Presets.themes[0]
        set(value) {
            field = value
            setTextColor(value.syntax.plain)
            lineNumbersBackgroundPaint.color = value.lineNumberBackground
            lineNumbersStrokePaint.color = value.lineNumberStroke
            lineNumbersTextPaint.color = value.lineNumberText
            selectedLinePaint.color = value.selectedLineBackground

            afterTextChanged(editableText) // refresh highlight
        }

    private val bufferRect = Rect()

    var lines: List<String> = emptyList()
        private set(value) {
            field = value
            linesWithNumbers = layout?.let(this::countNumbersForLines) ?: emptyList()
        }

    var linesWithNumbers: List<Int> = emptyList()
        private set

    
    private fun countNumbersForLines(layout: Layout) : List<Int> {
        var pos = 0
        return lines.map {
            val line = layout.getLineForOffset(pos)
            pos += it.length + 1
            line
        }
    }


    override fun onDraw(canvas: Canvas) {
        val lineNumbersOffset = lineNumbersTextPaint.measureText(lineCount.toString()).toInt() + 2 * LINE_NUMBERS_MARGIN_PX

        if (paddingLeft != lineNumbersOffset + LINE_NUMBERS_MARGIN_PX) {
            setPadding(lineNumbersOffset + LINE_NUMBERS_MARGIN_PX, paddingTop, paddingRight, paddingBottom)
        }

        canvas.drawRect(0f, 0f, lineNumbersOffset.toFloat(), height.toFloat(), lineNumbersBackgroundPaint) // line numbers bg
        canvas.drawLine(lineNumbersOffset.toFloat(), 0f, lineNumbersOffset.toFloat(), height.toFloat(), lineNumbersStrokePaint) // line numbers stroke

        if (layout != null) {
            if (linesWithNumbers.isEmpty() && lines.isNotEmpty()) { // layout could be null when lines is set so we have to check and recount line numbers in such case
                linesWithNumbers = countNumbersForLines(layout)
            }

            linesWithNumbers.forEachIndexed { lineNumber, line ->
                val y = getLineBounds(line, bufferRect)
                canvas.drawText((lineNumber + 1).toString(), lineNumbersOffset.toFloat() - LINE_NUMBERS_MARGIN_PX, y.toFloat(), lineNumbersTextPaint)
            }

            drawHighlightForCurrentLine(layout, canvas)
        }

        super.onDraw(canvas)
    }

    private fun drawHighlightForCurrentLine(layout: Layout, canvas: Canvas) {
        val cursorPosition = selectionStart
        if (cursorPosition > 0) {
            val selectedLine = layout.getLineForOffset(cursorPosition)
            var lineStart = selectedLine
            var lineEnd = selectedLine + 1

            while (!linesWithNumbers.contains(lineStart) && lineStart > 0) lineStart--
            while (!linesWithNumbers.contains(lineEnd) && lineEnd < lineCount) lineEnd++

            lineEnd -= 1

            getLineBounds(lineStart, bufferRect)
            val top = bufferRect.top.toFloat()

            getLineBounds(lineEnd, bufferRect)
            val bottom = bufferRect.bottom.toFloat()

            canvas.drawRect(0f, top, width.toFloat(), bottom, selectedLinePaint)
        }
    }


    private var insertedStart: Int = 0
    private var insertedCount: Int = 0

    override fun afterTextChanged(editable: Editable) {
        lines = text.toString().lines()
        CodeAnalyzer.onTextInserted(insertedStart, insertedCount, this)
        highlightPublisher.onNext(editable)
        requestLayout()
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, count: Int) {}

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, count: Int) {
        insertedStart = start
        insertedCount = count
    }


    private fun getFirstVisibleLine() = scrollContainer?.let {
        return@let layout.getLineForVertical(Math.max(0, it.scrollY - top))
    } ?: 0


    private fun getLastVisibleLine() = scrollContainer?.let {
        return@let layout.getLineForVertical(Math.max(0, it.scrollY + it.height - top))
    } ?: lineCount - 1


    private fun updateHighlight(results: List<ParseResult>) = layout?.let { layout ->
        val start = layout.getLineStart(getFirstVisibleLine())
        val end = layout.getLineEnd(getLastVisibleLine())
        removeSpans()
        setSpans(start, end, results)
    }

    private fun removeSpans() =
        editableText.getSpans(0, editableText.length, ParcelableSpan::class.java).forEach {
            editableText.removeSpan(it)
        }

    private fun setSpans(start: Int, end: Int, parseResults: List<ParseResult>) {
        parseResults
                .filterNot { it.offset + it.length < start || it.offset > end }
                .filter { theme.syntax.shouldBePainted(it.styleKeysString) }
                .forEach { pr ->
                    theme.syntax.colorMap[pr.styleKeysString]?.let {
                        editableText.setSpan(ForegroundColorSpan(it), pr.offset, Math.min(pr.offset + pr.length, editableText.length), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
    }
}