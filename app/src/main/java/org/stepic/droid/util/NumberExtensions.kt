package org.stepic.droid.util

infix fun Long.safeDiv(d: Long): Long =
    if (d == 0L) {
        0
    } else {
        this / d
    }