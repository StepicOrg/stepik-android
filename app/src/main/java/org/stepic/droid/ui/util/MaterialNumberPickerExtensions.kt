package org.stepic.droid.ui.util

import android.widget.NumberPicker
import biz.kasual.materialnumberpicker.MaterialNumberPicker

fun MaterialNumberPicker.initForCodeLanguages(languageNames: Array<String>) {
    this.minValue = 0
    this.maxValue = languageNames.size - 1
    this.displayedValues = languageNames
    this.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    this.wrapSelectorWheel = false

    try {
        this.setTextSize(50f) //Warning: reflection!
    } catch (exception: Exception) {
        //reflection failed -> ignore
    }
}