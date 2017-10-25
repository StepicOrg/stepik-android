package org.stepic.droid.util

import io.reactivex.Observable

enum class RxEmpty { INSTANCE }

data class RxOptional<out T> (val value: T?) {
    fun <R> map(f: (T) -> R?) =
            RxOptional(value?.let(f))
}

fun <T> Observable<RxOptional<T>>.unwrapOptional(): Observable<T> =
        this.filter { it.value != null }.map { it.value }