package org.stepic.droid.util

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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

class RetryExponential(private val maxAttempts: Int)
    : io.reactivex.functions.Function<Flowable<out Throwable>, Flowable<*>> {

    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> =
            attempts.zipWith(Flowable.range(1, maxAttempts), BiFunction { t1: Throwable, t2: Int -> handleRetryAttempt(t1, t2) })

    private fun handleRetryAttempt(throwable: Throwable, attempt: Int): Single<Long> =
            when (attempt) {
                1 -> Single.just(1L)
                maxAttempts -> Single.error<Long>(throwable)
                else -> {
                    val expDelay = Math.pow(2.toDouble(), (attempt - 2).toDouble()).toLong()
                    Single.timer(expDelay, TimeUnit.SECONDS)
                }
            }

}