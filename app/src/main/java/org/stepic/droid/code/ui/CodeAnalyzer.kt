package org.stepic.droid.code.ui

import org.stepic.droid.util.countWhile
import org.stepic.droid.util.substringOrNull

/**
 * Class for smart code analyzing
 */
object CodeAnalyzer {
    private const val LINE_BREAK = '\n'

    private val brackets = hashMapOf(
        "{" to "}",
        "(" to ")",
        "[" to "]"
    )

    private val quotes = hashSetOf(
        "\"", "'"
    )

    private fun getIndentForCurrentLine(start: Int, string: String) : Int {
        val prevLineStart = string.substring(0, start).lastIndexOf(LINE_BREAK)
        return string.countWhile (prevLineStart + 1) { Character.isWhitespace(it) && it != LINE_BREAK }
    }

    fun getIndentForLines(lines: List<String>) =
            lines.map { getIndentForCurrentLine(0, it) }.filter { it > 0 }.min() ?: CodeEditor.DEFAULT_INDENT_SIZE

    private fun getPrevSymbol(start: Int, string: String) : String? =
            string.substringOrNull(start - 1, start)

    private fun getNextSymbol(start: Int, string: String) : String? =
            string.substringOrNull(start, start + 1)


    fun onTextInserted(start: Int, count: Int, codeEditor: CodeEditor) {
        val inserted = codeEditor.editableText.toString().substring(start, start + count)
        val text = codeEditor.editableText.toString()
        when (inserted) {
            LINE_BREAK.toString() -> {
                val indent = getIndentForCurrentLine(start, text)

                val prev = getPrevSymbol(start, text)
                val next = getNextSymbol(start + 1, text)

                codeEditor.editableText.insert(start + count, " ".repeat(indent))

                if (prev in brackets) {
                    if (next != null && brackets[prev] == next) {
                        codeEditor.editableText.insert(start + count + indent, LINE_BREAK.toString())
                        codeEditor.setSelection(start + count + indent)
                    }
                    codeEditor.editableText.insert(start + count, " ".repeat(codeEditor.indentSize))
                }
            }

            in brackets -> {
                val next = getNextSymbol(start + 1, text)
                if (next == null || Character.isWhitespace(next[0]) || next in brackets.values) { // don't want auto bracket if there is a statement next
                    codeEditor.editableText.insert(start + count, brackets[inserted])
                    codeEditor.setSelection(start + count)
                }
            }

            in quotes -> {
                val next = getNextSymbol(start + 1, text)
                if (next == null || Character.isWhitespace(next[0])) { // don't want auto quote if there is a statement next
                    codeEditor.editableText.insert(start, inserted)
                    codeEditor.setSelection(start + count)
                }
            }
        }
    }

    fun findSecondBracket(bracket: String, start: Int, string: String) : Int {
        getBracketsPair(bracket)?.let {
            val delta = if (it.key == bracket) 1 else -1 // if bracket is opening delta will be positive otherwise negative
            var deep = 1
            var pos = start + delta

            while (0 <= pos && pos < string.length) {
                when (string.substring(pos, pos + 1)) {
                    it.key   -> deep += delta  // opening bracket
                    it.value -> deep -= delta  // closing bracket
                }

                if (deep == 0)
                    return pos

                pos += delta
            }
        }
        return -1
    }

    fun getBracketsPair(bracket: String) = brackets.entries.find { it.key == bracket || it.value == bracket }
}