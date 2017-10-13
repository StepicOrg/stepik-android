package org.stepic.droid.util

import android.widget.EditText


fun EditText.insertText(text: String) {
    val start = Math.max(this.selectionStart, 0)
    val end = Math.max(this.selectionEnd, 0)
    this.text.replace(start, end, text, 0, text.length)
}