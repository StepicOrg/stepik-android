package org.stepic.droid.util.rx

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper

class ObservableReduceMap<T : Any, R : Any>(
    private val source: ObservableSource<T>,
    private val seed: R,
    private val transform: (R, T) -> R
) : Observable<R>() {
    override fun subscribeActual(observer: Observer<in R>) {
        source.subscribe(ReduceMapObserver(observer, seed, transform))
    }

    private class ReduceMapObserver<T : Any, R : Any>(
        private val downstream: Observer<in R>,
        private var value: R,
        private val transform: (R, T) -> R
    ) : Observer<T>, Disposable {
        private var upstream: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d
                downstream.onSubscribe(this)
            }
        }

        override fun onNext(t: T) {
            val newValue = transform(value, t)
            downstream.onNext(newValue)
            value = newValue
        }

        override fun onError(e: Throwable) {
            downstream.onError(e)
        }

        override fun onComplete() {
            downstream.onComplete()
        }

        override fun dispose() {
            upstream?.dispose()
        }

        override fun isDisposed(): Boolean =
            upstream?.isDisposed ?: false
    }
}