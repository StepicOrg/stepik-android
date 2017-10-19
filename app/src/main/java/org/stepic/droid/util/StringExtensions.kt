package org.stepic.droid.util


/*
 * Counts while predicate is true
 */
inline fun String.countWhile(startIndex: Int = 0, predicate: (Char) -> Boolean) : Int {
    var pos = startIndex
    while (pos >= 0 && pos < this.length && predicate(this[pos])) pos++
    return pos - startIndex
}

fun String.substringOrNull(start: Int, end: Int = length) =
    if (0 <= start && end <= length) {
        substring(start, end)
    } else {
        null
    }
