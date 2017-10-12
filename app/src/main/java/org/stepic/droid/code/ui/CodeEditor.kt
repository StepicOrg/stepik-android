package org.stepic.droid.code.ui

import android.content.Context
import android.graphics.*
import android.support.v7.widget.AppCompatEditText
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ScrollView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.code.highlight.prettify.PrettifyParser
import org.stepic.droid.code.highlight.syntaxhighlight.ParseResult
import org.stepic.droid.code.highlight.themes.CodeTheme
import org.stepic.droid.code.highlight.themes.Presets.DefaultTheme
import org.stepic.droid.util.DpPixelsHelper
import java.util.concurrent.TimeUnit

class CodeEditor : AppCompatEditText, TextWatcher {
    companion object {
        const val SCROLL_DEBOUNCE_MS = 100L
        const val INPUT_DEBOUNCE_MS = 300L
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val parser = PrettifyParser()

    private val LINE_NUMBERS_MARGIN by lazy {
        DpPixelsHelper.convertDpToPixel(4f).toInt()
    }

    private val highlightPublisher = PublishSubject.create<Editable>()
    private val spanPublisher = BehaviorSubject.create<List<ParseResult>>()
    private val scrollPublisher = PublishSubject.create<Int>()
    private var scrollDisposable: Disposable? = null

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

    
    var scrollContainer: ScrollView? = null
        set(value) {
            field = value
            value?.let { container ->
                container.viewTreeObserver.addOnScrollChangedListener {
                    scrollPublisher.onNext(container.scrollY)
                }

                container.viewTreeObserver.addOnGlobalLayoutListener {
                    scrollPublisher.onNext(container.scrollY)
                }

                scrollDisposable = scrollPublisher
                    .debounce(SCROLL_DEBOUNCE_MS, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { spanPublisher.value?.let(this::updateHighlight) }
            }
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
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .subscribe(spanPublisher::onNext)
        )

        addTextChangedListener(this)
        afterTextChanged(editableText)
    }

    override fun onDetachedFromWindow() {
        removeTextChangedListener(this)
        compositeDisposable.dispose()
        scrollDisposable?.dispose()
        super.onDetachedFromWindow()
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

            afterTextChanged(editableText) // refresh highlight
        }

    private val bufferRect = Rect()

    var lines: List<String> = emptyList()
    var linesWithNumbers: List<Int> = emptyList()

    override fun onDraw(canvas: Canvas) {
        val lineNumbersOffset = lineNumbersTextPaint.measureText(lineCount.toString()).toInt() + 2 * LINE_NUMBERS_MARGIN

        setPadding(lineNumbersOffset + LINE_NUMBERS_MARGIN, paddingTop, paddingRight, paddingBottom)
        canvas.drawRect(0f, 0f, lineNumbersOffset.toFloat(), height.toFloat(), lineNumbersBackgroundPaint) // line numbers bg
        canvas.drawLine(lineNumbersOffset.toFloat(), 0f, lineNumbersOffset.toFloat(), height.toFloat(), lineNumbersStrokePaint) // line numbers stroke

        layout?.let { layout ->
            var pos = 0
            linesWithNumbers = lines.map {
                val line = layout.getLineForOffset(pos)
                pos += it.length + 1
                line
            }

            linesWithNumbers.forEachIndexed { lineNumber, line ->
                val y = getLineBounds(line, bufferRect)
                canvas.drawText(lineNumber.toString(), lineNumbersOffset.toFloat() - LINE_NUMBERS_MARGIN, y.toFloat(), lineNumbersTextPaint)
            }

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

        super.onDraw(canvas)
    }


    override fun afterTextChanged(editable: Editable) {
        lines = text.toString().lines()
        highlightPublisher.onNext(editable)
    }

    override fun beforeTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, count: Int) {}

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, count: Int) {}


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