package org.stepic.droid.util

import android.support.v4.util.LongSparseArray


fun <T: Comparable<T>> Array<T>.isOrdered(): Boolean =
        (0 until this.size - 1).none { this[it] > this[it + 1] }

fun <T: Comparable<T>> Array<T>.isNotOrdered(): Boolean =
        !this.isOrdered()

fun <T: Comparable<T>> Array<T>.isOrderedDesc(): Boolean =
        (0 until this.size - 1).none { this[it] < this[it + 1] }


fun <E> LongSparseArray<E>.putIfAbsent(key: Long, default: E) {
    if (this[key] == null) {
        this.put(key, default)
    }
}