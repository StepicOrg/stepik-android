package org.stepic.droid.code.ui

import android.util.Log


/**
 * Class for smart code analyzing
 */
class CodeAnalyzer {
    private val pairedChars = arrayOf('{', '}', '[', ']', '(', ')')

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


//    fun onTextInserted(inserted: String, pos: Int, codeEditor: CodeEditor) {
//        when (inserted) {
//            "\n" -> {
//                val line = getCurrentLine(codeEditor.lines, pos)
//                val indent = getIndentForLine(codeEditor.lines, line)
//
//                codeEditor.editableText.insert(pos + inserted.length, " ".repeat(indent))
//            }
//        }
//    }


    fun isPairedChar(char: Char) = char in pairedChars
}