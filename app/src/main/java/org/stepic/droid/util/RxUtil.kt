package org.stepic.droid.util

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.zipWith
import org.stepic.droid.util.rx.ObservableReduceMap
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

fun <T> Single<RxOptional<T>>.unwrapOptional(): Maybe<T> =
        this.filter { it.value != null }.map { it.value }

fun <T, R> Single<T>.mapNotNull(transform: (T) -> R?): Maybe<R> =
        this.map { RxOptional(transform(it)) }.unwrapOptional()

infix fun Completable.then(completable: Completable): Completable = this.andThen(completable)
infix fun <T> Completable.then(observable: Observable<T>): Observable<T> = this.andThen(observable)
infix fun <T> Completable.then(single: Single<T>): Single<T> = this.andThen(single)

infix fun <T> Observable<T>.merge(observable: Observable<T>): Observable<T> = Observable.merge(this, observable)
infix fun <T> Observable<T>.concat(observable: Observable<T>): Observable<T> = Observable.concat(this, observable)
operator fun <T> Observable<T>.plus(observable: Observable<T>): Observable<T> = merge(observable)

infix fun <X, Y> Observable<X>.zip(observable: Observable<Y>): Observable<Pair<X, Y>> = this.zipWith(observable)

class RetryWithDelay(private val retryDelayMillis: Int) : io.reactivex.functions.Function<Flowable<out Throwable>, Flowable<*>> {

    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> =
            attempts.flatMap { Flowable.timer(retryDelayMillis.toLong(), TimeUnit.MILLISECONDS) }
}

class RetryExponential(private val maxAttempts: Int)
    : io.reactivex.functions.Function<Flowable<out Throwable>, Flowable<*>> {

    override fun apply(attempts: Flowable<out Throwable>): Flowable<*> =
            attempts.zipWith(Flowable.range(1, maxAttempts), BiFunction { t1: Throwable, t2: Int -> handleRetryAttempt(t1, t2) })
                    .flatMap { x -> x.toFlowable() }

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

/**
 * Performs reduce operation with [seed] over values in [sources] and emits every obtained value
 */
fun <T : Any, R : Any> reduce(sources: List<Single<T>>, seed: R, transform: (R, T) -> Single<R>): Observable<R> =
    if (sources.isNotEmpty()) {
        sources
            .first()
            .flatMapObservable { item ->
                transform(seed, item)
                    .flatMapObservable { value ->
                        Observable.just(value) + reduce(sources.subList(1, sources.size), value, transform)
                    }
            }
    } else {
        Observable.empty<R>()
    }

/**
 * Performs reduce operation with [seed] over values in current observable and emits every obtained value
 */
fun <T : Any, R : Any> Observable<T>.reduceMap(seed: R, transform: (R, T) -> R): Observable<R> =
    ObservableReduceMap<T, R>(this, seed, transform)


fun <T : Any> ObservableEmitter<T>.onErrorSafe(throwable: Throwable) {
    if (!isDisposed) {
        onError(throwable)
    }
}
