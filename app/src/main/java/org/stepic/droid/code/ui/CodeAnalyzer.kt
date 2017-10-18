package org.stepic.droid.code.ui

/**
 * Class for smart code analyzing
 */
object CodeAnalyzer {
    private val brackets = hashMapOf(
        "{" to "}",
        "(" to ")",
        "[" to "]"
    )

    private val pairedChars = hashMapOf(
        "\"" to "\"",
        "'"  to "'"
    ) + brackets

    private inline fun String.countWhile(start: Int = 0, predicate: (Char) -> Boolean) : Int {
        var pos = start
        while (pos >= 0 && pos < this.length && predicate(this[pos])) pos++
        return pos - start
    }

    private fun getIndentForCurrentLine(start: Int, string: String) : Int {
        val prevLineStart = string.substring(0, start).lastIndexOf('\n')
        return string.countWhile (prevLineStart + 1) { Character.isWhitespace(it) && it != '\n' }
    }

    private fun getPrevSymbol(start: Int, string: String) : String? =
            if (start > 0)
                string.substring(start - 1, start)
            else null


    private fun getNextSymbol(start: Int, string: String) : String? =
            if (start + 1 <= string.length)
                string.substring(start, start + 1)
            else null



    private const val TAB_SIZE = 2 // todo count tabs

    fun onTextInserted(start: Int, count: Int, codeEditor: CodeEditor) {
        val inserted = codeEditor.editableText.toString().substring(start, start + count)
        val text = codeEditor.editableText.toString()
        when (inserted) {
            "\n" -> {
                val indent = getIndentForCurrentLine(start, text)

                val prev = getPrevSymbol(start, text)
                val next = getNextSymbol(start + 1, text)

                codeEditor.editableText.insert(start + count, " ".repeat(indent))

                if (prev in brackets) {
                    if (next != null && brackets[prev] == next) {
                        codeEditor.editableText.insert(start + count + indent, "\n")
                        codeEditor.setSelection(start + count + indent)
                    }
                    codeEditor.editableText.insert(start + count, " ".repeat(TAB_SIZE))
                }
            }
            in brackets -> {
                val next = getNextSymbol(start + 1, text)
                if (next == null || Character.isWhitespace(next[0]) || next in brackets.values) { // don't want auto bracket if there is a statement next
                    codeEditor.editableText.insert(start + count, pairedChars[inserted])
                    codeEditor.setSelection(start + count)
                }
            }
        }
    }
}