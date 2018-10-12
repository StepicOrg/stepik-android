package org.stepic.droid.util

import android.widget.EditText


fun EditText.insertText(text: String, offset : Int) {
    val start = Math.max(this.selectionStart - offset, 0)
    val end = Math.max(this.selectionEnd, 0)
    this.text.replace(start, end, text, 0, text.length)
}