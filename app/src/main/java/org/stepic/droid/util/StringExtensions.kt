package org.stepic.droid.util


/*
 * Counts while predicate is true
 */
inline fun String.countWhile(start: Int = 0, predicate: (Char) -> Boolean) : Int {
    var pos = start
    while (pos >= 0 && pos < this.length && predicate(this[pos])) pos++
    return pos - start
}

fun String.substringOrNull(start: Int, end: Int = length) =
    if (0 <= start && end <= length) {
        substring(start, end)
    } else {
        null
    }
