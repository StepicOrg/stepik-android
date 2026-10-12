package org.stepik.android.testutil

import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.CopyOnWriteArrayList

class DeferredEventRecorder {
    private val recordedEvents = CopyOnWriteArrayList<String>()
    val events: List<String> get() = recordedEvents.toList()

    fun completable(name: String, operation: () -> Completable = { Completable.complete() }): Completable =
        Completable.defer {
            recordedEvents += name + ":subscribe"
            operation()
                .doOnComplete { recordedEvents += name + ":complete" }
                .doOnError { recordedEvents += name + ":error" }
        }

    fun <T : Any> single(name: String, operation: () -> Single<T>): Single<T> =
        Single.defer {
            recordedEvents += name + ":subscribe"
            operation()
                .doOnSuccess { recordedEvents += name + ":success" }
                .doOnError { recordedEvents += name + ":error" }
        }
}
