package org.stepic.droid.util


/*
 * Counts while predicate is true
 */
inline fun String.countWhile(startIndex: Int = 0, reversed: Boolean = false, predicate: (Char) -> Boolean): Int {
    val delta = if (reversed) -1 else 1
    var pos = startIndex
    while (pos >= 0 && pos < this.length && predicate(this[pos])) pos += delta
    return delta * (pos - startIndex)
}

fun String.substringOrNull(start: Int, end: Int = length): String? =
    if (0 <= start && end <= length && start < end) {
        substring(start, end)
    } else {
        null
    }


inline fun String.takeLastFromIndexWhile(pos: Int, predicate: (Char) -> Boolean): String? =
    this.substringOrNull(pos - this.countWhile(pos - 1, reversed = true, predicate = predicate), pos)