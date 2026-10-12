package org.stepik.android.testutil

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.rules.ExternalResource
import java.util.concurrent.CopyOnWriteArrayList

class RxTestRule(private val overrideSchedulers: Boolean = false) : ExternalResource() {
    val scheduler = TestScheduler()
    private val recordedErrors = CopyOnWriteArrayList<Throwable>()
    val errors: List<Throwable> get() = recordedErrors.toList()
    private val disposables = CompositeDisposable()

    override fun before() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
        recordedErrors.clear()
        RxJavaPlugins.setErrorHandler { recordedErrors += it }
        if (overrideSchedulers) {
            RxJavaPlugins.setIoSchedulerHandler { scheduler }
            RxJavaPlugins.setComputationSchedulerHandler { scheduler }
            RxJavaPlugins.setNewThreadSchedulerHandler { scheduler }
            RxJavaPlugins.setSingleSchedulerHandler { scheduler }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { scheduler }
            RxAndroidPlugins.setMainThreadSchedulerHandler { scheduler }
        }
    }

    fun <T : Disposable> track(disposable: T): T {
        disposables.add(disposable)
        return disposable
    }

    fun advance() = scheduler.triggerActions()

    override fun after() {
        try {
            disposables.clear()
        } finally {
            // Reset Android first, even if Java teardown is reached after a failed assertion.
            RxAndroidPlugins.reset()
            RxJavaPlugins.reset()
        }
    }
}
