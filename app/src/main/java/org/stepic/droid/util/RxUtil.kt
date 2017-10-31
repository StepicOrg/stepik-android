package org.stepic.droid.util

import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.concurrent.TimeUnit


enum class RxEmpty { INSTANCE }

data class RxOptional<out T>(val value: T?) {
    fun <R> map(f: (T) -> R?) =
            RxOptional(value?.let(f))
}

fun <T> Observable<RxOptional<T>>.unwrapOptional(): Observable<T> =
        this.filter { it.value != null }.map { it.value }

fun <T> Flowable<RxOptional<T>>.unwrapOptional(): Flowable<T> =
        this.filter { it.value != null }.map { it.value }

class RetryWithDelay(private val retryDelayMillis: Int) : io.reactivex.functions.Function<Flowable<out Throwable>, Flowable<*>> {
    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> =
            attempts.flatMap { Flowable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS) }
}