package org.stepik.android.testutil

import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RxInfrastructureTest {
    @get:Rule
    val rx = RxTestRule()

    @Test
    fun deferredRecorderDistinguishesAssemblySubscriptionAndCompletion() {
        val recorder = DeferredEventRecorder()
        val gate = CompletableSubject.create()
        val save = recorder.completable("save") { gate }
        val verify = recorder.single("verify") { Single.just("verified") }
        val consume = recorder.completable("consume")
        val chain = save.andThen(verify).flatMapCompletable { consume }
        assertEquals(emptyList<String>(), recorder.events)

        val observer = rx.track(chain.test())
        assertEquals(listOf("save:subscribe"), recorder.events)
        observer.assertNotComplete()
        gate.onComplete()
        observer.assertComplete().assertNoErrors()
        assertEquals(
            listOf("save:subscribe", "save:complete", "verify:subscribe", "verify:success", "consume:subscribe", "consume:complete"),
            recorder.events
        )
    }

    @Test
    fun deferredFailureSkipsDownstreamSubscriptions() {
        val recorder = DeferredEventRecorder()
        val error = IllegalStateException("synthetic save failure")
        val save = recorder.completable("save") { io.reactivex.Completable.error(error) }
        val verify = recorder.single("verify") { Single.just("verified") }
        rx.track(save.andThen(verify).test()).assertError(error)
        assertEquals(listOf("save:subscribe", "save:error"), recorder.events)
        assertEquals(emptyList<Throwable>(), rx.errors)
    }

    @Test
    fun optInSchedulerAndErrorOverridesResetEvenAfterAssertionFailure() {
        val rule = RxTestRule(overrideSchedulers = true)
        val error = RuntimeException("synthetic plugin error")
        val assertion = AssertionError("synthetic test failure")
        var observer: io.reactivex.observers.TestObserver<String>? = null
        val statement = object : Statement() {
            override fun evaluate() {
                assertSame(rule.scheduler, Schedulers.io())
                assertSame(rule.scheduler, RxAndroidPlugins.initMainThreadScheduler { Schedulers.trampoline() })
                assertSame(rule.scheduler, RxAndroidPlugins.onMainThreadScheduler(Schedulers.trampoline()))
                RxJavaPlugins.onError(error)
                assertSame(error, (rule.errors.single() as io.reactivex.exceptions.UndeliverableException).cause)
                observer = rule.track(Single.just("scheduled").observeOn(rule.scheduler).test())
                observer!!.assertNoValues()
                rule.advance()
                observer!!.assertResult("scheduled")
                throw assertion
            }
        }
        try {
            rule.apply(statement, Description.createTestDescription(javaClass, "teardown")).evaluate()
            fail("Expected the synthetic assertion")
        } catch (actual: AssertionError) {
            assertSame(assertion, actual)
        }
        assertEquals(true, observer!!.isDisposed)
        assertNull(RxJavaPlugins.getErrorHandler())
        assertNull(RxJavaPlugins.getIoSchedulerHandler())
        assertNull(RxJavaPlugins.getComputationSchedulerHandler())
        assertNull(RxJavaPlugins.getNewThreadSchedulerHandler())
        assertNull(RxJavaPlugins.getSingleSchedulerHandler())
        assertNull(RxAndroidPlugins.getInitMainThreadSchedulerHandler())
        assertSame(Schedulers.trampoline(), RxAndroidPlugins.onMainThreadScheduler(Schedulers.trampoline()))
    }
}
