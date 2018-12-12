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


inline fun <T> List<T>.mapToLongArray(transform: (T) -> Long): LongArray {
    val array = LongArray(this.size)
    forEachIndexed { index, t ->
        array[index] = transform(t)
    }
    return array
}

inline fun <T> Array<T>.mapToLongArray(transform: (T) -> Long): LongArray {
    val array = LongArray(this.size)
    forEachIndexed { index, t ->
        array[index] = transform(t)
    }
    return array
}