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


fun LongArray?.isNullOrEmpty(): Boolean =
    this == null || this.isEmpty()


@JvmName("Iterable_LongArray__flatten")
fun Iterable<LongArray>.flatten(): LongArray {
    val size = sumBy { it.size }
    val array = LongArray(size)
    var offset = 0
    forEach { subArray ->
        subArray.copyInto(array, destinationOffset = offset)
        offset += subArray.size
    }

    return array
}

@JvmName("LongArray_distinct")
fun LongArray.distinct(): LongArray =
    toMutableSet().toLongArray()