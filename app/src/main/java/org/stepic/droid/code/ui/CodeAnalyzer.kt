package org.stepic.droid.code.ui

import android.util.Log


/**
 * Class for smart code analyzing
 */
object CodeAnalyzer {
//    private val pairedChars = arrayOf('{', '}', '[', ']', '(', ')')

    private val brackets = hashMapOf(
        "{" to "}",
        "(" to ")",
        "[" to "]"
    )

    private val pairedChars = hashMapOf(
        "\"" to "\"",
        "'"  to "'"
    ) + brackets

    private fun countIndent(line: String) =
            line.takeWhile { Character.isWhitespace(it) }.length

    private fun getCurrentLine(lines: List<String>, pos: Int) : Int {
        var p = pos
        var line = 0
        while (line < lines.size && p > lines[line].length) {
            p -= lines[line].length + 1
            line++
        }
        return line
    }

    private fun getIndentForLine(lines: List<String>, line: Int) = countIndent(lines[line])

    private fun getPrevSymbol(code: String, pos: Int) : String =
            code.substring(pos - 1, pos)

    private fun getNextSymbol(code: String, pos: Int) : String =
            code.substring(pos, pos + 1)

    fun onTextInserted(inserted: String, pos: Int, codeEditor: CodeEditor) {
        when (inserted) {
            "\n" -> {
                val line = getCurrentLine(codeEditor.lines, pos)
                val indent = getIndentForLine(codeEditor.lines, line)

                val prev = getPrevSymbol(codeEditor.text.toString(), pos)
                val next = getNextSymbol(codeEditor.text.toString(), pos + inserted.length)
                codeEditor.editableText.insert(pos + inserted.length, " ".repeat(indent))

                Log.d(javaClass.canonicalName, "pos $pos")
                Log.d(javaClass.canonicalName, "prev '$prev' + next '$next'")

                val br = brackets[prev]
                if (br == next) {
                    codeEditor.editableText.insert(pos + indent + inserted.length, "\n")
                    codeEditor.editableText.insert(pos + indent + inserted.length, "  ")
                    codeEditor.setSelection(codeEditor.selectionStart - 1)
                }
            }
            in pairedChars.keys -> {
                codeEditor.editableText.insert(pos + inserted.length, pairedChars[inserted])
                codeEditor.setSelection(pos + inserted.length)
            }
        }
    }
}