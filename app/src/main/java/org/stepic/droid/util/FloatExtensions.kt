package org.stepic.droid.util

import java.text.DecimalFormat

private const val FORMAT_PATTERN = "#"

fun Float.toFixed(length: Int): String {
    val decimalFormat = DecimalFormat("0.${FORMAT_PATTERN.repeat(length)}")
    return decimalFormat.format(this)
}